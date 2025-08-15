package stories

import db.Entity
import db.Id
import klite.jdbc.UpdatableEntity
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
  val velocityAveragedOver: Int = 3,
  val velocity: Int = 10,
  val currentIterationNum: Int = 1,
  val reviewTypes: Set<String> = setOf("Test (QA)", "Design", "Code", "Security"),
  val tags: Set<String> = emptySet(), // TODO: persist to db
  val defaultStoryPoints: Int? = 1,
  override var updatedAt: Instant? = null,
  val createdAt: Instant = nowSec(),
): Entity<Project>, UpdatableEntity
