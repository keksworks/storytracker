package stories

import auth.Access
import db.Id
import klite.AssetsHandler
import klite.HttpExchange
import klite.annotations.GET
import klite.annotations.POST
import klite.annotations.PathParam
import klite.annotations.QueryParam
import klite.jdbc.StaleEntityException
import klite.jdbc.nowSec
import stories.Story.Status.UNSCHEDULED
import stories.Story.Status.UNSTARTED
import users.Role.ADMIN
import users.Role.OWNER

@Access(ADMIN, OWNER)
class ProjectRoutes(
  private val projectRepository: ProjectRepository,
  private val storyRepository: StoryRepository,
  private val attachmentRepository: AttachmentRepository,
): AssetsHandler(attachmentRepository.path) {
  @GET fun list() = projectRepository.list()
  @GET("/:id") fun get(@PathParam id: Id<Project>) = projectRepository.get(id)

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

  /*
   *   1:after
   *   2:after1 (after, unchanged)
   *   4:after3 -> 2 (story)
   *   3:after2 -> 4 (newAfter)
   *   5:after4 -> 3 (prevAfter)
   */
  @POST("/:id/stories/move") fun moveStory(@PathParam id: Id<Project>, req: StoryMoveRequest): List<Story> {
    var story = storyRepository.get(req.storyId)
    require(story.projectId == id) { "Invalid story project" }
    val after = req.afterId?.let { storyRepository.get(it) }
    var newAfter = storyRepository.by(Story::afterId to req.afterId, Story::projectId to id) ?: error("No after story in project $id with id ${req.afterId}")
    var prevAfter = storyRepository.by(Story::afterId to story.afterId, Story::projectId to id)

    newAfter = newAfter.copy(afterId = story.afterId).also { storyRepository.save(it) }
    prevAfter = prevAfter?.copy(afterId = newAfter.id)?.also { storyRepository.save(it) }

    val newStatus = if (after?.status == UNSCHEDULED) UNSCHEDULED
                    else if (story.status == UNSCHEDULED) UNSTARTED else story.status
    story = story.copy(afterId = req.afterId, status = newStatus).also { storyRepository.save(it) }
    return listOfNotNull(story, newAfter, prevAfter)
  }

  @GET("/:id/stories/:storyId/attachments/:fileName")
  fun attachment(@PathParam id: Id<Project>, @PathParam storyId: Id<Story>, @PathParam fileName: String, e: HttpExchange) {
    send(e, attachmentRepository.file(id, storyId, fileName))
  }
}

data class StoryMoveRequest(val storyId: Id<Story>, val afterId: Id<Story>?)
