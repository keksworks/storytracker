package stories

import db.CrudRepository
import db.Entity
import db.Id
import klite.jdbc.nowSec
import java.time.DayOfWeek
import java.time.Instant
import javax.sql.DataSource

data class Project(
  override val id: Id<Project> = Id(),
  val name: String,
  val description: String?,
  val startDay: DayOfWeek,
  val iterationWeeks: Int = 1,
  val bugsEstimatable: Boolean = false,
  val timezone: String,
  val velocityAveragedWeeks: Int = 3,
  val version: Int = 0,
  val iterations: Int = 0,
  val updatedAt: Instant? = nowSec(),
  val createdAt: Instant = nowSec(),
): Entity<Project>//, UpdatableEntity

class ProjectRepository(db: DataSource): CrudRepository<Project>(db, "projects")
