package stories

import auth.Access
import auth.user
import db.Id
import klite.AssetsHandler
import klite.Before
import klite.ForbiddenException
import klite.HttpExchange
import klite.annotations.*
import klite.jdbc.StaleEntityException
import klite.jdbc.nowSec
import stories.Story.Status.DELETED
import users.Role.*

@Access(ADMIN, OWNER, VIEWER)
class ProjectRoutes(
  private val projectRepository: ProjectRepository,
  private val storyRepository: StoryRepository,
  private val projectMemberRepository: ProjectMemberRepository,
  private val attachmentRepository: AttachmentRepository,
): AssetsHandler(attachmentRepository.path), Before {
  override suspend fun before(e: HttpExchange) {
    e.path("id")?.let {
      val role = if (e.user.role == OWNER) OWNER else projectMemberRepository.role(Id(it), e.user.id)
      if (role == null) throw ForbiddenException()
      e.attr("role", role)
    }
  }

  @GET fun list() = projectRepository.list()

  @GET("/:id") fun get(@PathParam id: Id<Project>) = projectRepository.get(id)

  @GET("/:id/members") fun members(@PathParam id: Id<Project>): List<ProjectMemberUser> =
    projectMemberRepository.listWithUsers(id)

  @GET("/:id/stories") fun stories(@PathParam id: Id<Project>, @QueryParam fromIteration: Int? = null, @QueryParam q: String? = null) =
    storyRepository.list(id, fromIteration, q)

  @POST("/:id/stories") fun save(@PathParam id: Id<Project>, story: Story): Story {
    require(story.projectId == id) { "Invalid story project" }
    val existing = storyRepository.by(Story::id to story.id)
    if (existing != null && existing.updatedAt != story.updatedAt) throw StaleEntityException() // TODO: maybe implement UpdatableEntity
    // TODO update only changed fields (send only changed fields from UI, receive as a Map)
    return story.copy(updatedAt = nowSec()).also {
      storyRepository.save(it, skipUpdate = if (existing != null) emptySet() else setOf(Story::afterId))
    }
  }

  @DELETE("/:id/stories/:storyId") fun delete(@PathParam id: Id<Project>, @PathParam storyId: Id<Story>) {
    val story = storyRepository.get(storyId)
    require(story.projectId == id) { "Invalid story project" }
    storyRepository.save(story.copy(status = DELETED, updatedAt = nowSec()))
  }

  @GET("/:id/stories/:storyId/attachments/:fileName")
  fun attachment(@PathParam id: Id<Project>, @PathParam storyId: Id<Story>, @PathParam fileName: String, e: HttpExchange) {
    send(e, attachmentRepository.file(id, storyId, fileName))
  }
}
