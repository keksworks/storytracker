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
import klite.jdbc.eq
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import users.User
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class GitHubWebhookRoutesTest: BaseMocks() {
  val routes = create<GitHubWebhookRoutes>()

  private val defaultCommit = GitHubCommit("abc123", "#${story.id.value} Fix the login bug", "https://github.com/org/repo/commit/abc123")

  private fun pushPayload(vararg commits: GitHubCommit = arrayOf(defaultCommit), ref: String = "refs/heads/main") =
    GitHubPushPayload(ref = ref, repository = GitHubRepository("org/repo"), commits = commits.toList())

  private fun signed(payload: GitHubPushPayload) = signed(routes.jsonMapper.render(payload))
  private fun signed(json: String) = "sha256=${hmacSHA256(json, project.webhookSecret.toString())}"

  private fun handle(payload: GitHubPushPayload = pushPayload()) =
    routes.handle(project.id, routes.jsonMapper.render(payload), signed(payload))

  @Test fun `rejects missing signature`() {
    val ex = assertThrows<BadRequestException> { routes.handle(project.id, "{}", null) }
    expect(ex.message).toEqual("Invalid signature")
  }

  @Test fun `rejects invalid signature`() {
    assertThrows<BadRequestException> { routes.handle(project.id, "{}", "sha256=invalid") }
  }

  @Test fun `ignores non-main branch push`() {
    handle(pushPayload(ref = "refs/heads/feature"))

    verify(exactly = 0) { storyRepository.get(any()) }
  }

  @Test fun `adds comment for commit starting with story id on main branch`() {
    val commitUrl = "https://github.com/org/repo/commit/abc123"
    every { storyRepository.get(story.id) } returns story
    every { userRepository.by(User::email eq Email("dev@example.com")) } returns null

    handle(pushPayload(defaultCommit.copy(
      author = GitHubAuthor(email = "dev@example.com"),
    )))

    verify {
      storyRepository.save(match { savedStory ->
        savedStory.comments.size == 1 &&
          savedStory.comments[0].text!!.contains("org/repo") &&
          savedStory.comments[0].text!!.contains("Fix the login bug") &&
          savedStory.comments[0].text!!.contains(commitUrl)
      })
    }
  }

  @Test fun `matches commit author to existing user`() {
    every { storyRepository.get(story.id) } returns story
    every { userRepository.by(User::email eq user.email) } returns user

    handle(pushPayload(defaultCommit.copy(
      author = GitHubAuthor(email = user.email.value),
    )))

    verify {
      storyRepository.save(match { it.comments[0].createdBy == user.id })
    }
  }

  @Test fun `falls back to story createdBy when author email not matched`() {
    every { storyRepository.get(story.id) } returns story.copy(createdBy = user.id)
    every { userRepository.by(User::email eq Email("nobody@example.com")) } returns null

    handle(pushPayload(defaultCommit.copy(
      author = GitHubAuthor(email = "nobody@example.com"),
    )))

    verify {
      storyRepository.save(match { it.comments[0].createdBy == user.id })
    }
  }

  @Test fun `ignores commit without story id prefix`() {
    handle(pushPayload(defaultCommit.copy(
      message = "Just a regular commit",
    )))

    verify(exactly = 0) { storyRepository.save(any()) }
  }

  @Test fun `ignores commit with non-existent story id`() {
    every { storyRepository.get(Id<Story>(99999)) } throws RuntimeException("Not found")

    handle(pushPayload(defaultCommit.copy(
      message = "#99999 Fix something",
    )))

    verify(exactly = 0) { storyRepository.save(any()) }
  }

  @Test fun `uses compare URL when commit URL is missing`() {
    val compareUrl = "https://github.com/org/repo/compare/old...new"
    every { storyRepository.get(story.id) } returns story

    handle(pushPayload(GitHubCommit(
      id = "abc123",
      message = "#${story.id.value} Added feature",
    )).copy(compare = compareUrl))

    verify {
      storyRepository.save(match { it.comments[0].text!!.contains(compareUrl) })
    }
  }

  private fun hmacSHA256(data: String, secret: String): String {
    val mac = Mac.getInstance("HmacSHA256").apply { init(SecretKeySpec(secret.toByteArray(), "HmacSHA256")) }
    return mac.doFinal(data.toByteArray()).toHexString()
  }
}
