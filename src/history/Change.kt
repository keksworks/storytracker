package history

import db.Id
import users.User
import java.time.Instant

data class Change(
  val table: String,
  val rowId: Id<Any>,
  val old: Map<String, String?>,
  val new: Map<String, String?>,
  val changedAt: Instant,
  val changedBy: Id<User>?,
)
