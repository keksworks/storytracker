package db

import klite.Email
import klite.TSID
import stories.Project
import stories.Story
import users.Role.ADMIN
import users.Role.VIEWER
import users.User
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicLong

/** Immutable domain object samples for unit tests */
object TestData {
  init {
    TSID.deterministic = AtomicLong(171717188819)
  }

  val date = LocalDate.of(2025, 3, 3)

  val user = User("Test Admin", Email("pivotal@codeborne.com"), ADMIN)
  val viewer = User("Test Viewer", Email("viewer@codeborne.com"), VIEWER)

  val project = Project(Id(1), "Project 1", "Description")
  val story = Story(Id(), Id(1), "Story 1")
  val story2 = Story(Id(), Id(1), "Story 2", afterId = story.id)
}
