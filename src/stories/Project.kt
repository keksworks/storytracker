package stories

import db.Entity
import db.Id
import klite.jdbc.UpdatableEntity
import klite.jdbc.nowSec
import java.time.DayOfWeek
import java.time.Instant

data class Project(
  override val id: Id<Project> = Id(),
  val name: String,
  val description: String,
  val iterationWeeks: Int,
  val startDay: DayOfWeek,
  override var updatedAt: Instant? = nowSec(),
  val createdAt: Instant = nowSec(),
): Entity<Project>, UpdatableEntity
