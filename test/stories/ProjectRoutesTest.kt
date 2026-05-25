package stories

import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import db.BaseMocks
import db.Id
import db.TestData.admin
import db.TestData.change
import db.TestData.epic
import db.TestData.iteration
import db.TestData.project
import db.TestData.projectMemberUser
import db.TestData.story
import db.TestData.story2
import db.TestData.user
import email.EmailContent
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import users.Role.MEMBER
import users.Role.OWNER
import users.User

class ProjectRoutesTest: BaseMocks() {
  val routes = create<ProjectRoutes>()

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

  @Test fun `delete project`() {
    routes.deleteProject(project.id)
    verify { projectRepository.delete(project.id) }
  }

  @Test fun members() {
    every { projectMemberRepository.listWithUsers(project.id) } returns listOf(projectMemberUser)
    val result = routes.members(project.id)
    expect(result).toEqual(listOf(projectMemberUser))
  }

  @Test fun saveMember() {
    val request = ProjectMemberRequest(user.email, MEMBER, "nimi", "TU")
    every { userRepository.by(User::email to request.email) } returns user
    val result = routes.saveMember(project.id, request, exchange)
    expect(result.user.email).toEqual(user.email)
    expect(result.user.name).toEqual("nimi")
    expect(result.member.role).toEqual(MEMBER)

    verify {
      userRepository.save(result.user)
      projectMemberRepository.save(result.member)
      emailSender.send(user.email, any<EmailContent>())
    }
  }

  @Test fun `saveMember does not send invitation for existing member`() {
    val existingMember = projectMemberUser.member
    val request = ProjectMemberRequest(user.email, OWNER, user.name, user.initials ?: "", existingMember.id)
    every { projectMemberRepository.get(existingMember.id) } returns existingMember
    every { userRepository.get(user.id) } returns user
    val result = routes.saveMember(project.id, request, exchange)
    expect(result.member.role).toEqual(OWNER)

    verify(exactly = 0) { emailSender.send(any(), any()) }
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
    val result = routes.saveEpic(project.id, epic, "user 123")
    expect(result).toEqual(epic)
    verify { epicRepository.save(result) }
  }

  @Test fun deleteEpic() {
    routes.deleteEpic(project.id, epic.id, "user 123")
    verify { epicRepository.delete(epic.id) }
  }

  @Test fun iterations() {
    expect(routes.iterations(project.id)).toEqual(listOf(iteration))
  }

  @Test fun `updateIteration creates new when no existing`() {
    every { iterationRepository.get(project.id, project.currentIterationNum) } returns null
    every { iterationRepository.get(project.id, project.currentIterationNum - 1) } returns null

    routes.updateIteration(project.id, project.currentIterationNum, TeamStrengthRequest(80))

    verify { iterationRepository.save(match { it.teamStrength == 80 && it.number == project.currentIterationNum }) }
  }

  @Test fun `updateIteration uses previous iteration endDate as startDate`() {
    val prevIteration = iteration.copy(number = project.currentIterationNum - 1)
    every { iterationRepository.get(project.id, project.currentIterationNum) } returns null
    every { iterationRepository.get(project.id, project.currentIterationNum - 1) } returns prevIteration

    routes.updateIteration(project.id, project.currentIterationNum, TeamStrengthRequest(60))

    verify { iterationRepository.save(match { it.startDate == prevIteration.endDate && it.endDate == prevIteration.endDate.plusWeeks(project.iterationWeeks.toLong()) }) }
  }

  @Test fun `updateIteration updates teamStrength of existing record`() {
    every { iterationRepository.get(project.id, project.currentIterationNum) } returns iteration

    routes.updateIteration(project.id, project.currentIterationNum, TeamStrengthRequest(50))

    verify { iterationRepository.save(match { it.teamStrength == 50 && it.startDate == iteration.startDate }) }
  }

  @Test fun `updateIteration recalculates velocity from past iterations`() {
    val pastIteration = iteration.copy(number = 0, teamStrength = 80, acceptedPoints = 8)
    every { iterationRepository.get(project.id, 0) } returns pastIteration
    val savedIteration = pastIteration.copy(teamStrength = 50)
    every { iterationRepository.list(project.id, fromNumber = any()) } returns listOf(savedIteration)

    routes.updateIteration(project.id, 0, TeamStrengthRequest(50))

    verify { projectRepository.save(match { it.velocity == calculateVelocity(listOf(savedIteration)) }) }
  }

  @Test fun history() {
    expect(routes.history(project.id)).toEqual(listOf(change))
  }

  @Test fun stories() {
    val fromIteration = 2
    val beforeIteration = 1
    val q = "story"
    every { storyRepository.list(project.id, fromIteration, beforeIteration, q) } returns listOf(story2)
    val result = routes.stories(project.id, fromIteration, beforeIteration, q)
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
