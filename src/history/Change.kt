package history

import db.Id
import klite.jdbc.JsonColumn
import users.User
import java.time.Instant

data class Change(
  val table: String,
  val rowId: Id<Any>,
  @JsonColumn val old: Map<String, String?>,
  @JsonColumn val new: Map<String, String?>,
  val changedAt: Instant,
  val changedBy: Id<User>?,
)
