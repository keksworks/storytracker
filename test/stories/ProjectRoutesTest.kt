package stories

import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import db.BaseMocks
import db.TestData.admin
import db.TestData.epic
import db.TestData.project
import db.TestData.story
import db.TestData.story2
import db.TestData.user
import io.mockk.every
import org.junit.jupiter.api.Test

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
}
