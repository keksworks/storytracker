package stories

import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import db.BaseMocks
import db.Id
import db.TestData.admin
import db.TestData.epic
import db.TestData.iteration
import db.TestData.now
import db.TestData.project
import db.TestData.projectMemberUser
import db.TestData.story
import db.TestData.story2
import db.TestData.user
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.verify
import klite.Email
import klite.ForbiddenException
import klite.jdbc.eq
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import users.Role.MEMBER
import users.Role.OWNER
import users.User
import java.time.Instant.MIN

class ProjectImporterTestTest: BaseMocks() {
  val routes = create<ProjectImporter>()

  private val projectMemberUserNew = projectMemberUser.copy(user = user.copy(email = Email("new@user.com"), id = Id()))

  val export = ProjectExport(project, listOf(iteration), listOf(epic), listOf(story, story2), listOf(projectMemberUser, projectMemberUserNew))

  @Test fun `import new project allowed for everyone`() {
    every { projectRepository.get(project.id) } throws NoSuchElementException()
    every { userRepository.by(User::email eq projectMemberUser.user.email) } returns projectMemberUser.user
    every { projectMemberRepository.listWithUsers(project.id) } returns emptyList()
    every { epicRepository.list(project.id) } returns emptyList()
    every { storyRepository.list(project.id, any(), any()) } returns emptyList()
    every { iterationRepository.list(project.id) } returns emptyList()

    expect(routes.import(export, user)).toEqual(project)

    verify {
      projectRepository.create(project)
      iterationRepository.save(iteration)
      epicRepository.create(epic)
      storyRepository.create(story)
      storyRepository.create(story2)
      userRepository.create(projectMemberUserNew.user)
      projectMemberRepository.save(match { it.userId == user.id && it.role == OWNER })
      projectMemberRepository.create(projectMemberUser.member)
      projectMemberRepository.create(projectMemberUserNew.member)
    }
  }

  @Test fun `import of existing project forbidden for non-owner`() {
    every { projectMemberRepository.role(project.id, user.id) } returns MEMBER
    assertThrows<ForbiddenException> { routes.import(export, user) }
  }

  @Test fun `import of existing project allowed for admin`() {
    expect(routes.import(export, admin)).toEqual(project)
  }

  @Test fun `import of existing project allowed for owner`() {
    every { projectMemberRepository.role(project.id, user.id) } returns OWNER
    every { userRepository.by(User::email eq projectMemberUser.user.email) } returns projectMemberUser.user

    val storyToUpdate = story.copy(name = "updated name", updatedAt = now)
    val newStory = story2.copy(id = Id())
    val oldStory = story.copy(name = "old name", updatedAt = MIN)
    val epicToUpdate = epic.copy(name = "updated name", updatedAt = now)
    val newEpic = epic.copy(id = Id())
    val oldEpic = epic.copy(name = "old name", updatedAt = MIN)
    val newIteration = iteration.copy(projectId = Id(), number = 100)
    val customExport = export.copy(
      stories = listOf(storyToUpdate, newStory, oldStory), epics = listOf(epicToUpdate, newEpic, oldEpic),
      iterations = listOf(iteration, newIteration)
    )

    expect(routes.import(customExport, user)).toEqual(project)

    verify(exactly = 0) { projectRepository.save(any()) }
    verify {
      iterationRepository.save(newIteration)
      iterationRepository.list(projectId = project.id)
      storyRepository.list(export.project.id)
      storyRepository.save(storyToUpdate.copy(updatedAt = story.updatedAt))
      storyRepository.create(newStory)
      epicRepository.list(export.project.id)
      epicRepository.save(epicToUpdate.copy(updatedAt = epic.updatedAt))
      epicRepository.create(newEpic)
      userRepository.create(projectMemberUserNew.user)
      projectMemberRepository.create(match { it.userId == projectMemberUserNew.user.id && it.projectId == project.id })
    }
    confirmVerified(storyRepository, epicRepository)
  }
}
