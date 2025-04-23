package stories

import db.Id
import klite.Config
import klite.error
import klite.info
import klite.logger
import java.io.FileNotFoundException
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.div

class AttachmentRepository {
  private val path =  Path.of("attachments")
  private val cookie = Config.optional("PIVOTAL_COOKIE")
  private val log = logger()

  fun download(projectId: Id<Project>, ownerId: Id<out Any>, a: Story.Attachment, url: URI) {
    val file = (path / projectId.toString() / ownerId.toString() / a.filename).toFile()
    if (!file.exists() || file.length() < a.size) {
      file.parentFile.mkdirs()
      try {
        file.outputStream().use { out ->
          val conn = url.toURL().openConnection()
          cookie?.let { conn.setRequestProperty("Cookie", "t_session=$cookie") }
          conn.inputStream.use { it.copyTo(out) }
          log.info("Downloaded $file")
        }
      } catch (e: FileNotFoundException) {
        log.error("Failed to download ${a.filename}: $e")
        file.delete()
      }
    }
  }
}
