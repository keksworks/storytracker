package stories

import auth.Public
import db.Id
import klite.*
import klite.annotations.HeaderParam
import klite.annotations.POST
import klite.annotations.PathParam
import klite.jdbc.eq
import klite.json.JsonMapper
import users.User
import users.UserRepository
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.reflect.typeOf

@Public
class GitHubWebhookRoutes(
  private val projectRepository: ProjectRepository,
  private val storyRepository: StoryRepository,
  private val projectEvents: ProjectEvents,
  private val userRepository: UserRepository,
) {
  val log = logger()
  val jsonMapper = JsonMapper(keys = SnakeCase)
  private val commitRegex = Regex("^#(\\d+)\\s+(.*)", RegexOption.DOT_MATCHES_ALL)

  @POST("/projects/:id/github")
  fun handle(@PathParam id: Id<Project>, payload: String, @HeaderParam("X-Hub-Signature-256") signature: String?) {
    val project = projectRepository.get(id)
    verifySignature(payload, signature, project.webhookSecret.toString())

    val push = jsonMapper.parse<GitHubPushPayload>(payload, typeOf<GitHubPushPayload>())
    if (push.ref?.endsWith("/main") != true && push.ref?.endsWith("/master") != true) return

    val repoName = push.repository?.fullName ?: "unknown"

    push.commits.forEach { commit ->
      val message = commit.message.trim()
      val match = commitRegex.find(message) ?: return@forEach
      val storyId = match.groupValues[1].toLongOrNull() ?: return@forEach
      val commitSubject = match.groupValues[2].lineSequence().firstOrNull()?.take(200) ?: message.take(200)

      var story = try { storyRepository.get(Id(storyId)) } catch (_: Exception) {
        return@forEach log.warn("Commit $commitSubject references non-existent story #$storyId, skipping")
      }
      if (story.projectId != id) return@forEach log.warn("Story #$storyId is not of the expected project ${id}")

      val diffLink = commit.url ?: push.compare?.let { "$it#${commit.id}" }
      val fileStats = buildFileStats(commit)
      val text = """
        <a href="$diffLink">🔗 Commit from $repoName</a>
        $commitSubject
        $fileStats
      """.trimIndent()

      val createdBy = commit.author?.let {
        (userRepository.by(User::email eq it.email) ?: userRepository.by(User::name eq it.name))?.id
      } ?: story.createdBy

      val comment = Story.Comment(text, createdBy = createdBy)
      story = story.copy(comments = story.comments + comment)
      storyRepository.save(story)
      projectEvents.send(id, story, "story")
    }
  }

  private fun verifySignature(payload: String, signature: String?, secret: String) {
    val expectedSignature = hmacSHA256(payload, secret)
    if (signature == null || signature.removePrefix("sha256=") != expectedSignature) {
      throw BadRequestException("Invalid signature")
    }
  }

  private fun hmacSHA256(data: String, secret: String): String {
    val mac = Mac.getInstance("HmacSHA256").apply { init(SecretKeySpec(secret.toByteArray(), "HmacSHA256")) }
    return mac.doFinal(data.toByteArray()).toHexString()
  }

  private fun buildFileStats(commit: GitHubCommit): String {
    val added = commit.added?.size ?: 0
    val removed = commit.removed?.size ?: 0
    val modified = commit.modified?.size ?: 0
    val totalFiles = added + removed + modified
    if (totalFiles == 0) return ""

    val parts = mutableListOf<String>()
    if (added > 0) parts.add("+${added} file${if (added != 1) "s" else ""}")
    if (removed > 0) parts.add("-${removed} file${if (removed != 1) "s" else ""}")
    if (modified > 0) parts.add("~${modified} file${if (modified != 1) "s" else ""}")

    return "📄 ${parts.joinToString(" ")}"
  }

  private fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }
}

data class GitHubPushPayload(
  val ref: String?,
  val repository: GitHubRepository? = null,
  val compare: String? = null,
  val commits: List<GitHubCommit> = emptyList(),
)

data class GitHubRepository(
  val fullName: String,
)

data class GitHubCommit(
  val id: String,
  val message: String,
  val url: String? = null,
  val timestamp: String? = null,
  val author: GitHubAuthor? = null,
  val added: List<String>? = null,
  val removed: List<String>? = null,
  val modified: List<String>? = null,
)

data class GitHubAuthor(
  val email: Email? = null,
  val name: String? = null
)
