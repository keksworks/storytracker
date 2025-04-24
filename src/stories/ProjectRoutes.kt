package stories

import auth.Access
import db.Id
import klite.annotations.GET
import klite.annotations.PathParam
import klite.annotations.QueryParam
import users.Role.ADMIN
import users.Role.OWNER

@Access(ADMIN, OWNER)
class ProjectRoutes(
  private val projectRepository: ProjectRepository,
  private val storyRepository: StoryRepository,
  private val attachmentRepository: AttachmentRepository,
) {
  @GET fun list() = projectRepository.list()
  @GET("/:id") fun get(@PathParam id: Id<Project>) = projectRepository.get(id)

  @GET("/:id/stories") fun stories(@PathParam id: Id<Project>, @QueryParam fromIteration: Int? = null) =
    storyRepository.list(id, fromIteration)

  // @GET("/:id/:storyId/attachments/:name") fun attachment(@PathParam id: Id<Project>) =
}
