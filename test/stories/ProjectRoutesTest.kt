package stories

import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import db.BaseMocks
import db.Id
import db.TestData.admin
import db.TestData.change
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

class ProjectRoutesTest: BaseMocks() {
  val routes = create<ProjectRoutes>()
  private val projectMemberUserNew = projectMemberUser.copy(user = user.copy(email = Email("new@user.com"), id = Id()))

  val export = ProjectExport(project, listOf(iteration), listOf(epic), listOf(story, story2), listOf(projectMemberUser, projectMemberUserNew))

  @Test fun get() {
    expect(routes.get(project.id)).toEqual(project)
  }

  @Test fun `list for admin`() {
    val allProjects = listOf(project)
    every { projectRepository.list() } returns allProjects
    expect(routes.list(admin)).toEqual(allProjects)
  }

  @Test fun `list for member`() {
    val someProjects = listOf(project)
    every { projectRepository.listForMember(user.id) } returns someProjects
    expect(routes.list(user)).toEqual(someProjects)
  }

  @Test fun export() {
    val projectExport = ProjectExport(
      project,
      listOf(iteration),
      listOf(epic),
      listOf(story, story2),
      listOf(projectMemberUser)
    )
    expect(routes.export(project.id, exchange)).toEqual(projectExport)
  }

  @Test fun `import new project allowed for everyone`() {
    every { projectRepository.get(project.id) } throws NoSuchElementException()
    every { userRepository.by(User::email eq projectMemberUser.user.email) } returns projectMemberUser.user
    every { projectMemberRepository.listWithUsers(project.id) } returns emptyList()
    every { epicRepository.list(Epic::projectId to project.id) } returns emptyList()
    every { storyRepository.list(project.id, any(), any()) } returns emptyList()

    expect(routes.import(export, user, exchange)).toEqual(project)

    verify {
      projectRepository.save(project)
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
    assertThrows<ForbiddenException> { routes.import(export, user, exchange) }
  }

  @Test fun `import of existing project allowed for admin`() {
    expect(routes.import(export, admin, exchange)).toEqual(project)
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
    val customExport = export.copy(stories = listOf(storyToUpdate, newStory, oldStory), epics = listOf(epicToUpdate, newEpic, oldEpic))

    expect(routes.import(customExport, user, exchange)).toEqual(project)

    verify {
      projectRepository.save(project)
      iterationRepository.save(iteration)
      storyRepository.list(export.project.id)
      storyRepository.save(storyToUpdate.copy(updatedAt = story.updatedAt))
      storyRepository.create(newStory)
      epicRepository.list(Epic::projectId to export.project.id)
      epicRepository.save(epicToUpdate.copy(updatedAt = epic.updatedAt))
      epicRepository.create(newEpic)
      userRepository.create(projectMemberUserNew.user)
      projectMemberRepository.create(match{ it.userId == projectMemberUserNew.user.id && it.projectId == project.id })
    }
    confirmVerified(storyRepository, epicRepository)
  }

  @Test fun create() {
    val newProject = routes.create(project, user)
    expect(newProject).toEqual(project)
    verify {
      projectRepository.create(project)
      projectMemberRepository.save(match {
        it.projectId == project.id && it.userId == user.id && it.role == OWNER
      })
    }
  }

  @Test fun save() {
    val updatedProject = project.copy(name = "updated project name")
    val result = routes.save(updatedProject, project.id)
    expect(result).toEqual(updatedProject)
    verify { projectRepository.save(updatedProject) }
  }

  @Test fun `save with wrong id`() {
    val wrongId = Id<Project>()
    val projectWithWrongId = project.copy(id = wrongId)
    assertThrows<IllegalArgumentException> {
      routes.save(projectWithWrongId, project.id)
    }
  }

  @Test fun members() {
    every { projectMemberRepository.listWithUsers(project.id) } returns listOf(projectMemberUser)
    val result = routes.members(project.id)
    expect(result).toEqual(listOf(projectMemberUser))
  }

  @Test fun saveMember() {
    val request = ProjectMemberRequest(user.email, MEMBER, "nimi", "TU")
    every { userRepository.by(User::email to request.email) } returns user
    val result = routes.saveMember(project.id, request)
    expect(result.user.email).toEqual(user.email)
    expect(result.user.name).toEqual("nimi")
    expect(result.member.role).toEqual(MEMBER)

    verify {
      userRepository.save(result.user)
      projectMemberRepository.save(result.member)
    }
  }

  @Test fun addMeAsMember() {
    val result = routes.addMeAsMember(project.id, user)

    expect(result.user).toEqual(user)
    expect(result.member.projectId).toEqual(project.id)
    expect(result.member.userId).toEqual(user.id)
    expect(result.member.role).toEqual(MEMBER)

    verify { projectMemberRepository.save(result.member) }
  }

  @Test fun epics() {
    expect(routes.epics(project.id)).toEqual(listOf(epic))
  }

  @Test fun saveEpic() {
    val result = routes.saveEpic(project.id, epic)
    expect(result).toEqual(epic)
    verify { epicRepository.save(result) }
  }

  @Test fun deleteEpic() {
    routes.deleteEpic(project.id, epic.id)
    verify { epicRepository.delete(epic.id) }
  }

  @Test fun iterations() {
    expect(routes.iterations(project.id)).toEqual(listOf(iteration))
  }

  @Test fun history() {
    expect(routes.history(project.id)).toEqual(listOf(change))
  }

  @Test fun stories() {
    val fromIteration = 1
    val q = "story"
    every { storyRepository.list(project.id, fromIteration, q) } returns listOf(story2)
    val result = routes.stories(project.id, fromIteration, q)
    expect(result).toEqual(listOf(story2))
  }

  @Test fun `save story`() {
    val requesterId = "user 123"
    every { storyRepository.by(Story::id to story.id) } returns null
    val result = routes.save(project.id, story, requesterId)
    expect(result).toEqual(story)
    verify { storyRepository.save(result) }
  }

  @Test fun `save story with wrong project id`() {
    val wrongProjectId = Id<Project>()
    assertThrows<IllegalArgumentException> {
      routes.save(wrongProjectId, story, "user 123")
    }
  }

  @Test fun `delete story`() {
    val requesterId = "user 123"
    routes.delete(project.id, story.id, requesterId)
    verify { storyRepository.save(match { it.id == story.id && it.status == Story.Status.DELETED }) }
  }

  @Test fun attachment() {
    routes.attachment(project.id, story.id, "file", exchange)
    verify { attachmentRepository.file(project.id, story.id, "file") }
  }


}
