package stories

import ch.tutteli.atrium.api.fluent.en_GB.toBeEmpty
import ch.tutteli.atrium.api.fluent.en_GB.toContainExactly
import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import db.DBTest
import db.TestData.iteration
import db.TestData.project
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IterationRepositoryTest: DBTest() {
  val repository = IterationRepository(db)

  @BeforeEach fun setup() {
    ProjectRepository(db).save(project)
  }

  @Test fun `save and list`() {
    repository.save(iteration)

    expect(repository.list(project.id)).toContainExactly(iteration)
    expect(repository.list(project.id, fromNumber = iteration.number)).toContainExactly(iteration)
    expect(repository.list(project.id, fromNumber = iteration.number + 1)).toBeEmpty()

    expect(repository.get(project.id, iteration.number)).toEqual(iteration)
    expect(repository.get(project.id, 999)).toEqual(null)
  }
}
