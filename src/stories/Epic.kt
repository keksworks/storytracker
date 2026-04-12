package stories

import db.*
import db.CrudRepository
import db.Entity
import klite.jdbc.*
import klite.toValues
import stories.Story.Comment
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
  override var updatedAt: Instant? = null,
  val createdAt: Instant = nowSec(),
  val createdBy: Id<User>? = null,
  val deleted: Boolean = false,
): Entity<Epic>, UpdatableEntity

class EpicRepository(db: DataSource): CrudRepository<Epic>(db, "epics") {
  override fun Epic.persister() = toValues(Epic::comments to jsonb(comments), skip = listOf(Epic::deleted))
  override fun ResultSet.mapper() = create(Epic::comments to getJson<List<Comment>>("comments"))
  fun delete(id: Id<Epic>) = db.delete(table, Epic::id to id)

  fun list(projectId: Id<Project>) : List<Epic> = db.select(table,
    Epic::projectId to projectId,
    suffix = defaultOrder) { mapper() }
}
