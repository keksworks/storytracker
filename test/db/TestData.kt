package db

import klite.Email
import klite.TSID
import stories.Project
import stories.Story
import users.Role.ADMIN
import users.Role.VIEWER
import users.User
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZoneOffset.UTC
import java.util.concurrent.atomic.AtomicLong

/** Immutable domain object samples for unit tests */
object TestData {
  val date = LocalDate.of(2025, 3, 3)
  val now = date.atStartOfDay().toInstant(UTC)

  val user = User("Test Admin", Email("pivotal@codeborne.com"), ADMIN, createdAt = now, updatedAt = now)
  val viewer = User("Test Viewer", Email("viewer@codeborne.com"), VIEWER, createdAt = now, updatedAt = now)

  val project = Project(Id(1), "Project 1", "Description", createdAt = now, updatedAt = now)
  val story = Story(Id(), Id(1), "Story 1", createdAt = now, updatedAt = now)
  val story2 = Story(Id(), Id(1), "Story 2", afterId = story.id, createdAt = now, updatedAt = now)
}
