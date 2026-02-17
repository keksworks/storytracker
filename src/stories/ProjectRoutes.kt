package stories

import auth.Access
import auth.user
import db.Id
import klite.*
import klite.annotations.*
import klite.jdbc.*
import klite.sse.Event
import klite.sse.send
import klite.sse.startEventStream
import kotlinx.coroutines.flow.MutableSharedFlow
import stories.Story.Status.DELETED
import users.Role
import users.Role.*
import users.User
import users.UserRepository
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Access(ADMIN, OWNER, MEMBER, VIEWER)
class ProjectRoutes(
  private val projectRepository: ProjectRepository,
  private val storyRepository: StoryRepository,
  private val projectMemberRepository: ProjectMemberRepository,
  private val userRepository: UserRepository,
  private val epicRepository: EpicRepository,
  private val iterationRepository: IterationRepository,
  private val attachmentRepository: AttachmentRepository,
): AssetsHandler(attachmentRepository.path), Before {
  override suspend fun before(e: HttpExchange) {
    e.path("id")?.let {
      val role = if (e.user.role == OWNER) OWNER else projectMemberRepository.role(Id(it), e.user.id)
      if (role == null) throw ForbiddenException()
      e.attr("role", role)
    }
  }

  @GET fun list(@AttrParam user: User) =
    if (user.role == OWNER) projectRepository.list()
    else projectRepository.listForMember(user.id)

  @GET("/:id") fun get(@PathParam id: Id<Project>) = projectRepository.get(id)

  @POST @Access(ADMIN, OWNER)
  fun save(project: Project, @AttrParam user: User): Project {
    require(user.role == OWNER || projectMemberRepository.role(project.id, user.id) == OWNER) { "Not an owner" }
    projectRepository.save(project)
    // TODO: propagate to all members
    return project
  }

  @GET("/:id/members") fun members(@PathParam id: Id<Project>): List<ProjectMemberUser> =
    projectMemberRepository.listWithUsers(id)

  @POST("/:id/members") @Access(ADMIN, OWNER)
  fun addMember(@PathParam id: Id<Project>, req: ProjectMemberRequest): ProjectMemberUser {
    val user = userRepository.by(User::email eq req.email) ?:
    User(req.name, req.email, MEMBER, initials = req.initials).also { userRepository.save(it) }
    val member = ProjectMember(id, user.id, req.role).also { projectMemberRepository.save(it) }
    return ProjectMemberUser(member, user)
  }

  @POST("/:id/members/me") @Access(ADMIN, OWNER)
  fun addMeAsMember(@PathParam id: Id<Project>, @AttrParam user: User): ProjectMemberUser {
    val member = ProjectMember(id, user.id, MEMBER).also { projectMemberRepository.save(it) }
    return ProjectMemberUser(member, user)
  }

  @GET("/:id/epics") fun epics(@PathParam id: Id<Project>): List<Epic> =
    epicRepository.list(Epic::projectId to id)

  @GET("/:id/iterations") fun iterations(@PathParam id: Id<Project>): List<Iteration> =
    iterationRepository.list(projectId = id)

  @GET("/:id/stories") fun stories(@PathParam id: Id<Project>, @QueryParam fromIteration: Int? = null, @QueryParam q: String? = null) =
    storyRepository.list(id, fromIteration, q)

  @POST("/:id/stories") @Access(ADMIN, OWNER, MEMBER)
  fun save(@PathParam id: Id<Project>, story: Story, @HeaderParam requesterId: String): Story {
    require(story.projectId == id) { "Invalid story project" }
    val existing = storyRepository.by(Story::id to story.id)
    if (existing != null && existing.updatedAt != story.updatedAt) throw StaleEntityException() // TODO: maybe implement UpdatableEntity
    // TODO update only changed fields (send only changed fields from UI, receive as a Map)
    return story.copy(updatedAt = nowSec()).also {
      storyRepository.save(it, skipUpdate = setOf(Story::iteration))
      projectFlows[it.projectId]?.tryEmit(it to requesterId)
    }
  }

  private val projectFlows = ConcurrentHashMap<Id<Project>, MutableSharedFlow<Pair<Story, String>>>()

  @GET("/:id/updates/:requesterId") @NoTransaction
  suspend fun updates(@PathParam id: Id<Project>, @PathParam requesterId: String, e: HttpExchange) {
    val flow = projectFlows.getOrPut(id) { MutableSharedFlow(extraBufferCapacity = 1) }
    e.startEventStream()
    val after = e.header("Last-Event-ID")?.let { Instant.parse(it) }
    if (after != null) {
      val updatedSince = storyRepository.list(Story::projectId to id, Story::updatedAt gt after)
      updatedSince.forEach { story -> e.send(Event(story, "story", id = story.updatedAt)) }
    }
    flow.collect { (story, reqId) ->
      if (reqId != requesterId) e.send(Event(story, "story", id = story.updatedAt))
    }
  }

  @DELETE("/:id/stories/:storyId") @Access(ADMIN, OWNER, MEMBER)
  fun delete(@PathParam id: Id<Project>, @PathParam storyId: Id<Story>, @HeaderParam requesterId: String) {
    val story = storyRepository.get(storyId)
    require(story.projectId == id) { "Invalid story project" }
    story.copy(status = DELETED).also {
      storyRepository.save(it)
      projectFlows[it.projectId]?.tryEmit(it to requesterId)
    }
  }

  @GET("/:id/stories/:storyId/attachments/:fileName")
  fun attachment(@PathParam id: Id<Project>, @PathParam storyId: Id<Story>, @PathParam fileName: String, e: HttpExchange) {
    send(e, attachmentRepository.file(id, storyId, fileName))
  }
}

data class ProjectMemberRequest(val email: Email, val role: Role, val name: String, val initials: String)
