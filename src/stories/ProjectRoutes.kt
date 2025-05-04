package stories

import auth.Access
import auth.user
import db.Id
import klite.AssetsHandler
import klite.Before
import klite.ForbiddenException
import klite.HttpExchange
import klite.annotations.*
import klite.jdbc.NoTransaction
import klite.jdbc.StaleEntityException
import klite.jdbc.gt
import klite.jdbc.nowSec
import klite.sse.Event
import klite.sse.send
import klite.sse.startEventStream
import kotlinx.coroutines.flow.MutableSharedFlow
import stories.Story.Status.DELETED
import users.Role.*
import users.User
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Access(ADMIN, OWNER, VIEWER)
class ProjectRoutes(
  private val projectRepository: ProjectRepository,
  private val storyRepository: StoryRepository,
  private val projectMemberRepository: ProjectMemberRepository,
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

  @GET("/:id/members") fun members(@PathParam id: Id<Project>): List<ProjectMemberUser> =
    projectMemberRepository.listWithUsers(id)

  @GET("/:id/epics") fun epics(@PathParam id: Id<Project>): List<Epic> =
    epicRepository.list(Epic::projectId to id)

  @GET("/:id/iterations") fun iterations(@PathParam id: Id<Project>): List<Iteration> =
    iterationRepository.list(projectId = id)

  @GET("/:id/stories") fun stories(@PathParam id: Id<Project>, @QueryParam fromIteration: Int? = null, @QueryParam q: String? = null) =
    storyRepository.list(id, fromIteration, q)

  @POST("/:id/stories") fun save(@PathParam id: Id<Project>, story: Story): Story {
    require(story.projectId == id) { "Invalid story project" }
    val existing = storyRepository.by(Story::id to story.id)
    if (existing != null && existing.updatedAt != story.updatedAt) throw StaleEntityException() // TODO: maybe implement UpdatableEntity
    // TODO update only changed fields (send only changed fields from UI, receive as a Map)
    return story.copy(updatedAt = nowSec()).also {
      storyRepository.save(it, skipUpdate = setOf(Story::iteration))
      projectFlows[it.projectId]?.tryEmit(it)
    }
  }

  private val projectFlows = ConcurrentHashMap<Id<Project>, MutableSharedFlow<Story>>()

  @GET("/:id/updates") @NoTransaction
  suspend fun updates(@PathParam id: Id<Project>, @QueryParam after: Instant? = null, e: HttpExchange) {
    val flow = projectFlows.getOrPut(id) { MutableSharedFlow(extraBufferCapacity = 100) }
    e.startEventStream()
    if (after != null) {
      val updatedSince = storyRepository.list(Story::projectId to id, Story::updatedAt gt after)
      updatedSince.forEach { e.send(Event(it, "story")) }
    }
    flow.collect { e.send(Event(it, "story")) }
  }

  @DELETE("/:id/stories/:storyId") fun delete(@PathParam id: Id<Project>, @PathParam storyId: Id<Story>) {
    val story = storyRepository.get(storyId)
    require(story.projectId == id) { "Invalid story project" }
    storyRepository.save(story.copy(status = DELETED, updatedAt = nowSec()))
    // TODO emit
  }

  @GET("/:id/stories/:storyId/attachments/:fileName")
  fun attachment(@PathParam id: Id<Project>, @PathParam storyId: Id<Story>, @PathParam fileName: String, e: HttpExchange) {
    send(e, attachmentRepository.file(id, storyId, fileName))
  }
}
