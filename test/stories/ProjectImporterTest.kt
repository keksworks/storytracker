package stories

import ch.tutteli.atrium.api.fluent.en_GB.messageToContain
import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.fluent.en_GB.toThrow
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
import users.Role.MEMBER
import users.Role.OWNER
import users.User
import java.time.Instant.MIN

class ProjectImporterTest: BaseMocks() {
  val routes = create<ProjectImporter>()

  private val projectMemberUserNew = projectMemberUser.copy(user = user.copy(email = Email("new@user.com"), id = Id()))

  val export = ProjectExport(project, listOf(iteration), listOf(epic), listOf(story, story2), listOf(projectMemberUser, projectMemberUserNew))

  @Test fun `import new project allowed for everyone`() {
    val existingUserInSystem = user.copy(id = Id())
    val updatedStory = story.copy(assignedTo = user.id, createdBy = user.id)
    val updatedStory2 = story.copy(assignedTo = user.id, createdBy = user.id)
    val updatedEpic = epic.copy(createdBy = user.id)
    val testExport = export.copy(epics = listOf(updatedEpic), stories = listOf(updatedStory))

    every { projectRepository.get(project.id) } throws NoSuchElementException()
    every { userRepository.by(User::email eq projectMemberUser.user.email) } returns existingUserInSystem
    every { projectMemberRepository.listWithUsers(project.id) } returns emptyList()
    every { epicRepository.list(project.id) } returns emptyList()
    every { storyRepository.list(project.id, any(), any()) } returns emptyList()
    every { iterationRepository.list(project.id) } returns emptyList()

    expect(routes.import(testExport, user)).toEqual(project)

    verify {
      projectRepository.create(project)
      projectMemberRepository.save(match { it.userId == user.id && it.role == OWNER })
      projectMemberRepository.create(projectMemberUser.member.copy(userId = existingUserInSystem.id))
      projectMemberRepository.create(projectMemberUserNew.member.copy(userId = projectMemberUserNew.user.id))
      iterationRepository.save(iteration)
      epicRepository.create(updatedEpic.copy(createdBy = existingUserInSystem.id))
      storyRepository.create(updatedStory.copy(assignedTo = existingUserInSystem.id, createdBy = existingUserInSystem.id))
      storyRepository.create(updatedStory2.copy(assignedTo = existingUserInSystem.id, createdBy = existingUserInSystem.id))
      userRepository.create(projectMemberUserNew.user)
    }
  }

  @Test fun `import of existing project forbidden for non-owner`() {
    every { projectMemberRepository.role(project.id, user.id) } returns MEMBER
    expect { routes.import(export, user) }.toThrow<ForbiddenException>().messageToContain("projects.importForbidden")
  }

  @Test fun `import of existing project allowed for admin`() {
    expect(routes.import(export, admin)).toEqual(project)
  }

  @Test fun `import of existing project allowed for owner`() {
    val existingUserInSystem = projectMemberUser.user.copy(id = Id())

    every { projectMemberRepository.role(project.id, user.id) } returns OWNER
    every { userRepository.by(User::email eq projectMemberUser.user.email) } returns existingUserInSystem

    val storyToUpdate = story.copy(name = "updated name", updatedAt = now, assignedTo = user.id, createdBy = user.id)
    val newStory = story2.copy(id = Id(), assignedTo = user.id, createdBy = user.id)
    val oldStory = story.copy(name = "old name", updatedAt = MIN, assignedTo = user.id, createdBy = user.id)
    val epicToUpdate = epic.copy(name = "updated name", updatedAt = now, createdBy = user.id)
    val newEpic = epic.copy(id = Id(), createdBy = user.id)
    val oldEpic = epic.copy(name = "old name", updatedAt = MIN, createdBy = user.id)
    val newIteration = iteration.copy(projectId = Id(), number = 100)
    val testExport = export.copy(
      stories = listOf(storyToUpdate, newStory, oldStory), epics = listOf(epicToUpdate, newEpic, oldEpic),
      iterations = listOf(iteration, newIteration)
    )

    expect(routes.import(testExport, user)).toEqual(project)

    verify(exactly = 0) { projectRepository.save(any()) }
    verify {
      userRepository.create(projectMemberUserNew.user)
      projectMemberRepository.create(projectMemberUser.member.copy(userId = existingUserInSystem.id))
      projectMemberRepository.create(projectMemberUserNew.member.copy(userId = projectMemberUserNew.user.id))
      iterationRepository.save(newIteration)
      iterationRepository.list(projectId = project.id)
      storyRepository.list(export.project.id)
      storyRepository.save(storyToUpdate.copy(updatedAt = story.updatedAt,assignedTo = existingUserInSystem.id, createdBy = existingUserInSystem.id))
      storyRepository.create(newStory.copy(assignedTo = existingUserInSystem.id, createdBy = existingUserInSystem.id))
      epicRepository.list(export.project.id)
      epicRepository.save(epicToUpdate.copy(updatedAt = epic.updatedAt, createdBy = existingUserInSystem.id))
      epicRepository.create(newEpic.copy(createdBy = existingUserInSystem.id))
    }
    confirmVerified(storyRepository, epicRepository)
  }
}
