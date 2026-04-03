package stories

import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import db.BaseMocks
import db.Id
import db.TestData.project
import db.TestData.story
import db.TestData.user
import io.mockk.every
import io.mockk.verify
import klite.BadRequestException
import klite.Email
import klite.i18n.Lang
import klite.jdbc.eq
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import users.User
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class GitHubWebhookRoutesTest: BaseMocks() {
  val routes = create<GitHubWebhookRoutes>()

  @Test fun `rejects missing signature`() {
    val ex = assertThrows<BadRequestException> { routes.handle(project.id, "{}", null) }
    expect(ex.message).toEqual("Invalid signature")
  }

  @Test fun `rejects invalid signature`() {
    assertThrows<BadRequestException> { routes.handle(project.id, "{}", "sha256=invalid") }
  }

  @Test fun `ignores non-main branch push`() {
    val payload = """{"ref":"refs/heads/feature","commits":[]}"""
    val sig = hmacSHA256(payload, project.webhookSecret.toString())

    routes.handle(project.id, payload, "sha256=$sig")

    verify(exactly = 0) { storyRepository.get(any()) }
  }

  @Test fun `adds comment for commit starting with story id on main branch`() {
    val commitMessage = "#${story.id.value} Fix the login bug"
    val commitUrl = "https://github.com/org/repo/commit/abc123"
    val authorEmail = "dev@example.com"
    val payload = Lang.jsonMapper.render(mapOf(
      "ref" to "refs/heads/main",
      "repository" to mapOf("full_name" to "org/repo"),
      "compare" to "https://github.com/org/repo/compare/old...new",
      "commits" to listOf(mapOf(
        "id" to "abc123",
        "message" to commitMessage,
        "url" to commitUrl,
        "timestamp" to "2025-01-01T00:00:00Z",
        "author" to mapOf("email" to authorEmail),
      ))
    ))
    val sig = hmacSHA256(payload, project.webhookSecret.toString())
    every { storyRepository.get(story.id) } returns story
    every { userRepository.by(User::email eq Email(authorEmail)) } returns null

    routes.handle(project.id, payload, "sha256=$sig")

    verify {
      storyRepository.save(match { savedStory ->
        savedStory.comments.size == 1 &&
          savedStory.comments[0].text!!.contains("Commit from org/repo") &&
          savedStory.comments[0].text!!.contains("Fix the login bug") &&
          savedStory.comments[0].text!!.contains(commitUrl)
      })
    }
  }

  @Test fun `matches commit author to existing user`() {
    val commitMessage = "#${story.id.value} Fix the login bug"
    val commitUrl = "https://github.com/org/repo/commit/abc123"
    val payload = Lang.jsonMapper.render(mapOf(
      "ref" to "refs/heads/main",
      "repository" to mapOf("full_name" to "org/repo"),
      "commits" to listOf(mapOf(
        "id" to "abc123",
        "message" to commitMessage,
        "url" to commitUrl,
        "author" to mapOf("email" to user.email.value),
      ))
    ))
    val sig = hmacSHA256(payload, project.webhookSecret.toString())
    every { exchange.rawBody } returns payload
    every { storyRepository.get(story.id) } returns story
    every { userRepository.by(users.User::email eq user.email) } returns user

    routes.handle(project.id, payload, "sha256=$sig")

    verify {
      storyRepository.save(match { savedStory ->
        savedStory.comments[0].createdBy == user.id
      })
    }
  }

  @Test fun `falls back to story createdBy when author email not matched`() {
    val commitMessage = "#${story.id.value} Fix the login bug"
    val commitUrl = "https://github.com/org/repo/commit/abc123"
    val unknownEmail = "nobody@example.com"
    val payload = Lang.jsonMapper.render(mapOf(
      "ref" to "refs/heads/main",
      "repository" to mapOf("full_name" to "org/repo"),
      "commits" to listOf(mapOf(
        "id" to "abc123",
        "message" to commitMessage,
        "url" to commitUrl,
        "author" to mapOf("email" to unknownEmail),
      ))
    ))
    val sig = hmacSHA256(payload, project.webhookSecret.toString())
    every { exchange.rawBody } returns payload
    every { storyRepository.get(story.id) } returns story.copy(createdBy = user.id)
    every { userRepository.by(User::email eq Email(unknownEmail)) } returns null

    routes.handle(project.id, payload, "sha256=$sig")

    verify {
      storyRepository.save(match { savedStory ->
        savedStory.comments[0].createdBy == user.id
      })
    }
  }

  @Test fun `ignores commit without story id prefix`() {
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

    routes.handle(project.id, payload, "sha256=$sig")

    verify(exactly = 0) { storyRepository.save(any()) }
  }

  @Test fun `ignores commit with non-existent story id`() {
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
    every { storyRepository.get(Id(99999)) } throws RuntimeException("Not found")

    routes.handle(project.id, payload, "sha256=$sig")

    verify(exactly = 0) { storyRepository.save(any()) }
  }

  @Test fun `uses compare URL when commit URL is missing`() {
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
    every { storyRepository.get(story.id) } returns story

    routes.handle(project.id, payload, "sha256=$sig")

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
