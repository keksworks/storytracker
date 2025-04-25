package stories

import auth.Access
import db.Id
import klite.AssetsHandler
import klite.HttpExchange
import klite.annotations.GET
import klite.annotations.POST
import klite.annotations.PathParam
import klite.annotations.QueryParam
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

  @POST("/:id/stories") fun save(@PathParam id: Id<Project>, story: Story) {
    require(story.projectId == id) { "Invalid story projectId" }
    // TODO: protect with updatedAt checking
    // TODO: maybe handle afterId change and update other stories here?
    storyRepository.save(story)
  }

   @GET("/:id/stories/:storyId/attachments/:fileName")
   fun attachment(@PathParam id: Id<Project>, @PathParam storyId: Id<Story>, @PathParam fileName: String, e: HttpExchange) {
     send(e, attachmentRepository.file(id, storyId, fileName))
   }
}
