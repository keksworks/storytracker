package stories

import db.CrudRepository
import db.Id
import db.getJson
import db.jsonb
import klite.info
import klite.jdbc.*
import klite.logger
import klite.toValues
import java.sql.ResultSet
import java.time.Instant
import javax.sql.DataSource
import kotlin.reflect.KProperty1

class StoryRepository(db: DataSource): CrudRepository<Story>(db, "stories") {
  override val defaultOrder get() = "order by ord"

  init {
    val maxId = db.query("select max(id) from $table") { getLong(1) }.firstOrNull()
    if (maxId != null && maxId > Id.sequence.get()) {
      Id.sequence.set(maxId + (Math.random() * 1000).toLong())
    }
    logger().info("Initialized next story id to ${Id.sequence.get()}")
  }

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

  fun list(projectId: Id<Project>, fromIteration: Int? = null, q: String? = null): List<Story> {
    return db.select(table,
      Story::projectId to projectId,
      fromIteration?.let { Story::iteration to NullOrOp(">=", it) },
      q?.let { "%$q%" }?.let { or(Story::id to q.trimStart('#').toLongOrNull(), Story::tags any q, Story::name ilike it, Story::description ilike it, sql("comments::text ilike ?", it)) },
      suffix = defaultOrder) { mapper() }
  }

  fun save(story: Story, skipUpdate: Set<KProperty1<Story, *>>) =
    db.upsert(table, story.persister(), skipUpdateFields = skipUpdate.map { it.name }.toSet())

  fun lastUpdated(id: Id<Project>): Instant? =
    db.query("select max(updatedAt) as max from $table", Story::projectId to id) { getInstantOrNull("max") }.firstOrNull()
}
