package stories

import db.CrudRepository
import db.Entity
import db.Id
import db.getJson
import db.jsonb
import klite.jdbc.create
import klite.jdbc.nowSec
import klite.toValues
import stories.Story.Blocker
import stories.Story.Comment
import stories.Story.Task
import users.User
import java.sql.ResultSet
import java.time.Instant
import javax.sql.DataSource

data class Epic(
  override val id: Id<Epic> = Id(),
  val projectId: Id<Project>,
  val name: String,
  val description: String? = null,
  val tag: String,
  val comments: List<Comment> = emptyList(),
  val updatedAt: Instant? = nowSec(),
  val createdAt: Instant = nowSec(),
  val createdBy: Id<User>? = null,
): Entity<Epic>

class EpicRepository(db: DataSource): CrudRepository<Epic>(db, "epics") {
  override fun Epic.persister() = toValues(Epic::comments to jsonb(comments))
  override fun ResultSet.mapper() = create(Epic::comments to getJson<List<Comment>>("comments"))
}
