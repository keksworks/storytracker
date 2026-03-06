package db

import klite.Email
import stories.Project
import stories.Story
import users.User
import java.time.LocalDate
import java.time.ZoneOffset.UTC

/** Immutable domain object samples for unit tests */
object TestData {
  val date = LocalDate.of(2025, 3, 3)
  val now = date.atStartOfDay().toInstant(UTC)

  val admin = User("Test Admin", Email("admin@azib.net"), isAdmin = true, createdAt = now)
  val user = User("Test User", Email("user@azib.net"), createdAt = now)

  val project = Project(Id(1), "Project 1", "Description", createdAt = now)
  val story = Story(Id(), Id(1), "Story 1", createdAt = now)
  val story2 = Story(Id(), Id(1), "Story 2", order = 2.0, createdAt = now)
}
