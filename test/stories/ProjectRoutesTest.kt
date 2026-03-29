package stories

import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import db.BaseMocks
import db.Id
import db.TestData.admin
import db.TestData.epic
import db.TestData.project
import db.TestData.projectMemberUser
import db.TestData.story
import db.TestData.story2
import db.TestData.user
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import users.Role

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
      listOf(epic),
      listOf(story, story2)
    )
    expect(routes.export(project.id, exchange)).toEqual(projectExport)
  }

  @Test fun create() {
   val newProject = routes.create(project, user)
    expect(newProject).toEqual(project)
    verify { projectRepository.create(project) }
    verify {
      projectMemberRepository.save(match {
        it.projectId == project.id && it.userId == user.id && it.role == Role.OWNER
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
    val request = ProjectMemberRequest(
      email = user.email,
      role = Role.MEMBER,
      name = user.name,
      initials = "TU"
    )
    every { userRepository.by(any()) } returns user
    val result = routes.saveMember(project.id, request)
    expect(result.user.email).toEqual(user.email)
    expect(result.member.role).toEqual(Role.MEMBER)

    verify {userRepository.save(any()) }
    verify {projectMemberRepository.save(any()) }
  }

}
