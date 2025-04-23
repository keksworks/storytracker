package stories

import ch.tutteli.atrium.api.fluent.en_GB.toContain
import ch.tutteli.atrium.api.fluent.en_GB.toContainExactly
import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import db.DBTest
import db.TestData.project
import db.TestData.story
import db.TestData.story2
import org.junit.jupiter.api.Test

class StoryRepositoryTest: DBTest() {
  val repository = StoryRepository(db)

  @Test fun `save & load`() {
    ProjectRepository(db).save(project)
    repository.save(story)
    repository.save(story2)
    expect(repository.get(story.id)).toEqual(story)
    expect(repository.list(Story::projectId to project.id)).toContain(story, story2)
  }
}
