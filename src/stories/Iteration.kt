package stories

import db.Id
import java.time.LocalDate

data class Iteration(
  val projectId: Id<Project>,
  val number: Int,
  val length: Int = 1,
  val teamStrength: Int = 100,
  val startDate: LocalDate,
  val endDate: LocalDate,
  val points: Int? = null,
  val acceptedPoints: Int? = null,
  val velocity: Int = 10,
)
