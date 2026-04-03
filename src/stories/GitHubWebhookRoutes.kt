package stories

import auth.Public
import db.Id
import klite.BadRequestException
import klite.HttpExchange
import klite.annotations.POST
import klite.i18n.Lang
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.reflect.typeOf

@Public
class GitHubWebhookRoutes(
  private val projectRepository: ProjectRepository,
  private val storyRepository: StoryRepository,
  private val storyEvents: StoryEvents,
) {
  private val commitRegex = Regex("^#(\\d+)\\s+(.*)", RegexOption.DOT_MATCHES_ALL)

  @POST("/projects/:id/github")
  fun handle(e: HttpExchange) {
    val projectId = e.path("id")?.toLong()?.let { Id<Project>(it) } ?: throw BadRequestException("Missing project id")
    val project = projectRepository.get(projectId)
    val signature = e.header("X-Hub-Signature-256")
    val payload = e.rawBody
    verifySignature(payload, signature, project.webhookSecret.toString())

    val push = Lang.jsonMapper.parse<GitHubPushPayload>(payload, typeOf<GitHubPushPayload>())
    if (push.ref?.endsWith("/main") != true && push.ref?.endsWith("/master") != true) return

    val repoName = push.repository?.full_name ?: "unknown"

    push.commits.forEach { commit ->
      val message = commit.message.trim()
      val match = commitRegex.find(message) ?: return@forEach
      val storyId = match.groupValues[1].toLongOrNull() ?: return@forEach
      val commitSubject = match.groupValues[2].lineSequence().firstOrNull()?.take(200) ?: message.take(200)

      val story = try { storyRepository.get(Id(storyId)) } catch (_: Exception) { return@forEach }
      if (story.projectId != projectId) return@forEach

      val diffLink = commit.url ?: push.compare?.let { "$it#${commit.id}" }
      val commentText = buildString {
        append("🔗 **Commit from $repoName**\n\n")
        append("**$commitSubject**\n\n")
        if (diffLink != null) append("[View commit]($diffLink)")
      }

      val comment = Story.Comment(
        text = commentText,
        createdBy = story.createdBy ?: Id(),
      )
      val updatedStory = story.copy(comments = story.comments + comment)
      storyRepository.save(updatedStory)
      storyEvents.sendUpdates(projectId, updatedStory)
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
  val full_name: String,
)

data class GitHubCommit(
  val id: String,
  val message: String,
  val url: String?,
  val timestamp: String?,
)
