package stories

import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import db.BaseMocks
import db.Id
import db.TestData.project
import db.TestData.story
import io.mockk.every
import io.mockk.verify
import klite.BadRequestException
import klite.i18n.Lang
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class GitHubWebhookRoutesTest: BaseMocks() {
  val routes = create<GitHubWebhookRoutes>()

  init {
    every { exchange.path("id") } returns project.id.toString()
  }

  @Test
  fun `rejects missing signature`() {
    every { exchange.rawBody } returns "{}"
    every { exchange.header("X-Hub-Signature-256") } returns null

    val ex = assertThrows<BadRequestException> { routes.handle(exchange) }
    expect(ex.message).toEqual("Invalid signature")
  }

  @Test
  fun `rejects invalid signature`() {
    every { exchange.rawBody } returns "{}"
    every { exchange.header("X-Hub-Signature-256") } returns "sha256=invalid"

    assertThrows<BadRequestException> { routes.handle(exchange) }
  }

  @Test
  fun `ignores non-main branch push`() {
    val payload = """{"ref":"refs/heads/feature","commits":[]}"""
    val sig = hmacSHA256(payload, project.webhookSecret.toString())
    every { exchange.rawBody } returns payload
    every { exchange.header("X-Hub-Signature-256") } returns "sha256=$sig"

    routes.handle(exchange)

    verify(exactly = 0) { storyRepository.get(any()) }
  }

  @Test
  fun `adds comment for commit starting with story id on main branch`() {
    val commitMessage = "#${story.id.value} Fix the login bug"
    val commitUrl = "https://github.com/org/repo/commit/abc123"
    val payload = Lang.jsonMapper.render(mapOf(
      "ref" to "refs/heads/main",
      "repository" to mapOf("full_name" to "org/repo"),
      "compare" to "https://github.com/org/repo/compare/old...new",
      "commits" to listOf(mapOf(
        "id" to "abc123",
        "message" to commitMessage,
        "url" to commitUrl,
        "timestamp" to "2025-01-01T00:00:00Z"
      ))
    ))
    val sig = hmacSHA256(payload, project.webhookSecret.toString())
    every { exchange.rawBody } returns payload
    every { exchange.header("X-Hub-Signature-256") } returns "sha256=$sig"
    every { storyRepository.get(story.id) } returns story

    routes.handle(exchange)

    verify {
      storyRepository.save(match { savedStory ->
        savedStory.comments.size == 1 &&
          savedStory.comments[0].text!!.contains("Commit from org/repo") &&
          savedStory.comments[0].text!!.contains("Fix the login bug") &&
          savedStory.comments[0].text!!.contains(commitUrl)
      })
    }
  }

  @Test
  fun `ignores commit without story id prefix`() {
    val commitMessage = "Just a regular commit"
    val payload = Lang.jsonMapper.render(mapOf(
      "ref" to "refs/heads/main",
      "repository" to mapOf("full_name" to "org/repo"),
      "commits" to listOf(mapOf(
        "id" to "abc123",
        "message" to commitMessage,
        "url" to "https://github.com/org/repo/commit/abc123",
      ))
    ))
    val sig = hmacSHA256(payload, project.webhookSecret.toString())
    every { exchange.rawBody } returns payload
    every { exchange.header("X-Hub-Signature-256") } returns "sha256=$sig"

    routes.handle(exchange)

    verify(exactly = 0) { storyRepository.save(any()) }
  }

  @Test
  fun `ignores commit with non-existent story id`() {
    val commitMessage = "#99999 Fix something"
    val payload = Lang.jsonMapper.render(mapOf(
      "ref" to "refs/heads/master",
      "repository" to mapOf("full_name" to "org/repo"),
      "commits" to listOf(mapOf(
        "id" to "abc123",
        "message" to commitMessage,
        "url" to "https://github.com/org/repo/commit/abc123",
      ))
    ))
    val sig = hmacSHA256(payload, project.webhookSecret.toString())
    every { exchange.rawBody } returns payload
    every { exchange.header("X-Hub-Signature-256") } returns "sha256=$sig"
    every { storyRepository.get(Id<Story>(99999)) } throws RuntimeException("Not found")

    routes.handle(exchange)

    verify(exactly = 0) { storyRepository.save(any()) }
  }

  @Test
  fun `uses compare URL when commit URL is missing`() {
    val commitMessage = "#${story.id.value} Added feature"
    val compareUrl = "https://github.com/org/repo/compare/old...new"
    val payload = Lang.jsonMapper.render(mapOf(
      "ref" to "refs/heads/main",
      "repository" to mapOf("full_name" to "org/repo"),
      "compare" to compareUrl,
      "commits" to listOf(mapOf(
        "id" to "abc123",
        "message" to commitMessage,
      ))
    ))
    val sig = hmacSHA256(payload, project.webhookSecret.toString())
    every { exchange.rawBody } returns payload
    every { exchange.header("X-Hub-Signature-256") } returns "sha256=$sig"
    every { storyRepository.get(story.id) } returns story

    routes.handle(exchange)

    verify {
      storyRepository.save(match { savedStory ->
        savedStory.comments[0].text!!.contains(compareUrl)
      })
    }
  }

  private fun hmacSHA256(data: String, secret: String): String {
    val mac = Mac.getInstance("HmacSHA256").apply { init(SecretKeySpec(secret.toByteArray(), "HmacSHA256")) }
    return mac.doFinal(data.toByteArray()).joinToString("") { "%02x".format(it) }
  }
}
