package stories

import db.CrudRepository
import db.getJson
import db.jsonb
import klite.PropValue
import klite.jdbc.create
import klite.jdbc.query
import klite.toValues
import java.sql.ResultSet
import javax.sql.DataSource

class StoryRepository(db: DataSource): CrudRepository<Story>(db, "stories") {
  override fun Story.persister() = toValues(
    Story::tasks to jsonb(tasks),
    Story::comments to jsonb(comments),
    Story::blockers to jsonb(blockers),
  )
  override fun ResultSet.mapper() = create(
    Story::tasks to getJson<List<Story.Task>>("tasks"),
    Story::comments to getJson<List<Story.Comment>>("comments"),
    Story::blockers to getJson<List<Story.Blocker>>("blockers"),
  )

// TODO: recursive select is noticeable slower, maybe we could build the linked list in Kotlin
//  override fun list(vararg where: PropValue<Story>?, suffix: String): List<Story> =
//    db.query("""with recursive ordered_stories as (
//      select s.* from $table s where s.afterId is null
//      union all
//      select next.* from stories next join ordered_stories os on next.afterId = os.id
//    )
//    select * from ordered_stories""") { mapper() }
}
