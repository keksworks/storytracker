package stories

import db.Entity
import db.Id
import klite.jdbc.nowSec
import java.time.DayOfWeek
import java.time.DayOfWeek.MONDAY
import java.time.Instant

data class Project(
  override val id: Id<Project> = Id(),
  val name: String,
  val description: String?,
  val startDay: DayOfWeek = MONDAY,
  val iterationWeeks: Int = 1,
  val bugsEstimatable: Boolean = false,
  val timezone: String = "UTC",
  val velocityAveragedWeeks: Int = 3,
  val version: Int = 0,
  val iterations: Int = 0,
  val updatedAt: Instant? = nowSec(),
  val createdAt: Instant = nowSec(),
): Entity<Project>//, UpdatableEntity
