package stories

import auth.Public
import db.Id
import klite.*
import klite.annotations.HeaderParam
import klite.annotations.POST
import klite.annotations.PathParam
import klite.jdbc.eq
import klite.json.JsonMapper
import users.UserRepository
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.reflect.typeOf

@Public
class GitHubWebhookRoutes(
  private val projectRepository: ProjectRepository,
  private val storyRepository: StoryRepository,
  private val storyEvents: StoryEvents,
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

      val story = try { storyRepository.get(Id(storyId)) } catch (_: Exception) {
        return@forEach log.warn("Commit $commitSubject references non-existent story #$storyId, skipping")
      }
      if (story.projectId != id) return@forEach log.warn("Story #$storyId is not of the expected project ${id}")

      val diffLink = commit.url ?: push.compare?.let { "$it#${commit.id}" }
      val commentText = buildString {
        append("🔗 Commit from $repoName\n\n")
        append("$commitSubject\n\n")
        if (diffLink != null) append("""<a href="$diffLink">View in GitHub</a>""")
      }

      val createdBy = commit.author?.email?.let { email ->
        userRepository.by(users.User::email eq Email(email))?.id
      } ?: story.createdBy

      val comment = Story.Comment(commentText, createdBy = createdBy)
      val updatedStory = story.copy(comments = story.comments + comment)
      storyRepository.save(updatedStory)
      storyEvents.sendUpdates(id, updatedStory)
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

  private fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }
}

data class GitHubPushPayload(
  val ref: String?,
  val repository: GitHubRepository?,
  val compare: String?,
  val commits: List<GitHubCommit> = emptyList(),
)

data class GitHubRepository(
  val fullName: String,
)

data class GitHubCommit(
  val id: String,
  val message: String,
  val url: String?,
  val timestamp: String?,
  val author: GitHubAuthor?,
)

data class GitHubAuthor(
  val email: String?,
)
