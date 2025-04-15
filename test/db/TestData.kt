package db

import klite.Email
import klite.TSID
import users.Address
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

  val address = Address(
    id = "3429052",
    countryCode = "EE",
    county = "Harju maakond",
    municipality = "Rae vald",
    building = "1/3",
    postalCode = "75304",
    street = "Karusambla tee",
    details = "Karusambla tee 1/3, Rae vald"
  )

  val user = User("Test", "Admin", Email("admin@artun.ee"), ADMIN)
  val viewer = User("Test", "Admin", Email("admin@artun.ee"), VIEWER)
}
