package history

import ch.tutteli.atrium.api.fluent.en_GB.notToBeEmpty
import ch.tutteli.atrium.api.fluent.en_GB.notToEqual
import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import db.DBTest
import db.Id
import db.TestData.admin
import db.TestData.epic
import db.TestData.iteration
import db.TestData.project
import db.TestData.story
import org.junit.jupiter.api.Test
import stories.EpicRepository
import stories.IterationRepository
import stories.ProjectRepository
import stories.StoryRepository

class ChangeHistoryRepositoryTest: DBTest() {
  val repository = ChangeHistoryRepository(db)

  @Test fun `list returns changes for a project`() {
    ProjectRepository(db).save(project)
    project.copy(name = "Updated Project").also { ProjectRepository(db).save(it) }

    IterationRepository(db).save(iteration)
    StoryRepository(db).save(story)
    story.copy(name = "Updated Story").also { StoryRepository(db).save(it) }
    EpicRepository(db).save(epic)
    epic.copy(name = "Updated Epic").also { EpicRepository(db).save(it) }

    val changes = repository.list(project.id)
    expect(changes).notToBeEmpty()
  }

  @Test fun `change has old and new maps with only changed columns`() {
    ProjectRepository(db).save(project)
    project.copy(name = "Updated Project").also { ProjectRepository(db).save(it) }

    val changes = repository.list(project.id)
    expect(changes).notToBeEmpty()

    val change = changes.first()
    expect(change.old.size).toEqual(change.new.size)
  }

  @Test fun `change contains table and rowId`() {
    ProjectRepository(db).save(project)
    project.copy(name = "Updated Project").also { ProjectRepository(db).save(it) }

    val changes = repository.list(project.id)
    expect(changes).notToBeEmpty()

    val change = changes.find { it.table == "projects" }
    expect(change).notToEqual(null)
    expect(change!!.rowId).toEqual(project.id as Id<Any>)
    expect(change.table).toEqual("projects")
  }

  @Test fun `change records changedBy`() {
    ProjectRepository(db).save(project)
    project.copy(name = "Updated Project").also { ProjectRepository(db).save(it) }

    val changes = repository.list(project.id)
    expect(changes).notToBeEmpty()

    val change = changes.find { it.table == "projects" }
    expect(change).notToEqual(null)
    expect(change!!.changedBy).toEqual(admin.id)
  }

  @Test fun `list returns only changes for given project`() {
    ProjectRepository(db).save(project)
    project.copy(name = "Updated Project").also { ProjectRepository(db).save(it) }

    val changes = repository.list(project.id)
    expect(changes).notToBeEmpty()
  }
}
