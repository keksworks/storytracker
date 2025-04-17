package stories

import db.CrudRepository
import db.Entity
import db.Id
import klite.jdbc.nowSec
import users.User
import java.time.Instant
import javax.sql.DataSource

data class Epic(
  override val id: Id<Epic> = Id(),
  val projectId: Id<Project>,
  val name: String,
  val description: String? = null,
  val tag: String,
  val updatedAt: Instant? = nowSec(),
  val createdAt: Instant = nowSec(),
  val createdBy: Id<User>? = null,
): Entity<Epic>

class EpicRepository(db: DataSource): CrudRepository<Epic>(db, "epics")
