package stories

import auth.Public
import db.Id
import klite.annotations.GET
import klite.annotations.PathParam

@Public
class ProjectRoutes(
  val projectRepository: ProjectRepository,
  val storyRepository: StoryRepository,
) {
  @GET fun list() = projectRepository.list()
  @GET("/:id") fun get(@PathParam id: Id<Project>) = projectRepository.get(id)
  @GET("/:id/stories") fun stories(@PathParam id: Id<Project>) = storyRepository.list(Story::projectId to id)
}
