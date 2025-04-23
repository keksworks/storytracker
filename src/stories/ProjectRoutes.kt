package stories

import auth.Access
import db.Id
import klite.annotations.GET
import klite.annotations.PathParam
import users.Role.ADMIN
import users.Role.OWNER

@Access(ADMIN, OWNER)
class ProjectRoutes(
  val projectRepository: ProjectRepository,
  val storyRepository: StoryRepository,
) {
  @GET fun list() = projectRepository.list()
  @GET("/:id") fun get(@PathParam id: Id<Project>) = projectRepository.get(id)
  @GET("/:id/stories") fun stories(@PathParam id: Id<Project>) = storyRepository.list(id)
}
