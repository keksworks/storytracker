package stories

import db.Id
import users.User
import java.time.Instant

data class Change(
  val table: String,
  val rowId: Id<Any>,
  val column: String,
  val oldValue: String?,
  val newValue: String?,
  val changedAt: Instant,
  val changedBy: Id<User>?,
)
