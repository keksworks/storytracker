package stories

import ch.tutteli.atrium.api.fluent.en_GB.toBeEmpty
import ch.tutteli.atrium.api.fluent.en_GB.toContainExactly
import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import db.DBTest
import db.TestData.epic
import db.TestData.project
import org.junit.jupiter.api.Test

class EpicRepositoryTest: DBTest() {
  val repository = EpicRepository(db)

  @Test fun `save & load`() {
    ProjectRepository(db).save(project)
    repository.save(epic)
    expect(repository.get(epic.id)).toEqual(epic)
    expect(repository.list(Epic::projectId to project.id)).toContainExactly(epic)

    repository.delete(epic.id)
    expect(repository.list(Epic::projectId to project.id)).toBeEmpty()
  }
}
