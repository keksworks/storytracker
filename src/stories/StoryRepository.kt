package stories

import db.CrudRepository
import db.Id
import db.getJson
import db.jsonb
import klite.jdbc.*
import klite.toValues
import java.sql.ResultSet
import java.time.Instant
import javax.sql.DataSource

class StoryRepository(db: DataSource): CrudRepository<Story>(db, "stories") {
  override val defaultOrder get() = ""

  override fun Story.persister() = toValues(
    Story::tasks to jsonb(tasks),
    Story::comments to jsonb(comments),
    Story::blockers to jsonb(blockers),
    Story::reviews to jsonb(reviews),
  )
  override fun ResultSet.mapper() = create(
    Story::tasks to getJson<List<Story.Task>>("tasks"),
    Story::comments to getJson<List<Story.Comment>>("comments"),
    Story::blockers to getJson<List<Story.Blocker>>("blockers"),
    Story::reviews to getJson<List<Story.Review>>("reviews"),
  )

  fun setIteration(iteration: Iteration, storyIds: List<Int>) =
    if (storyIds.isEmpty()) 0 else
    db.update(table, mapOf(Story::iteration to iteration.number), Story::id to storyIds)

  fun list(projectId: Id<Project>, fromIteration: Int? = null): List<Story> {
    val condition = "s.projectId = $projectId"
    return db.query("""with recursive ordered as (
      select s.* from $table s where s.afterId is null and $condition
      union all
      select s.* from stories s join ordered os on s.afterId = os.id where $condition
    ) select * from ordered""",
      fromIteration?.let { Story::iteration to NullOrOp("=", it) }
    ) { mapper() }
  }

  fun lastUpdated(id: Id<Project>): Instant? =
    db.query("select max(updatedAt) as max from $table", Story::projectId to id) { getInstantOrNull("max") }.firstOrNull()
}
