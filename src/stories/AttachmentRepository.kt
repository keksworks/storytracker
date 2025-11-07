package stories

import db.Id
import klite.NotFoundException
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.exists

class AttachmentRepository {
  val path = Path.of("attachments")

  fun file(projectId: Id<Project>, storyId: Id<Story>, filename: String): Path {
    val file = (path / projectId.toString() / storyId.toString() / filename)
    if (!file.exists()) throw NotFoundException(filename)
    return file
  }
}
