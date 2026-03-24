package stories

import ch.tutteli.atrium.api.fluent.en_GB.toContainExactly
import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import db.DBTest
import db.Id
import db.TestData.iteration
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
    expect(repository.list(project.id)).toContainExactly(story, story2)
  }

  @Test fun reindexStoryOrder() {
    ProjectRepository(db).save(project)
    IterationRepository(db).save(iteration)
    val s0 = story.copy(id = Id(), name = "S0", order = 0.5, iteration = iteration.number).also { repository.save(it) }
    val s1 = story.copy(id = Id(), name = "S1", order = 10.5).also { repository.save(it) }
    val s2 = story.copy(id = Id(), name = "S2", order = 2.1).also { repository.save(it) }
    val s3 = story.copy(id = Id(), name = "S3", order = 5.0).also { repository.save(it) }

    repository.reindexStoryOrder(project.id)

    expect(repository.get(s0.id).order).toEqual(0.5)
    expect(repository.get(s2.id).order).toEqual(1.0)
    expect(repository.get(s3.id).order).toEqual(2.0)
    expect(repository.get(s1.id).order).toEqual(3.0)
  }
}
