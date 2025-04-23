package db

import klite.Email
import klite.TSID
import users.Role.*
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
}
