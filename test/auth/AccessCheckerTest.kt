package auth

import db.BaseMocks
import db.TestData.admin
import db.TestData.user
import io.mockk.every
import io.mockk.verify
import klite.ForbiddenException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import users.Role.ADMIN
import users.Role.VIEWER

class AccessCheckerTest: BaseMocks() {
  val checker = create<AccessChecker>()

  @Test fun `allows public access`() {
    every { exchange.session["userId"] } returns null
    every { exchange.route.annotations } returns listOf(Public())
    checker.before(exchange)
  }

  @Test fun `forbids unauthorized access`() {
    every { exchange.session["userId"] } returns null
    assertThrows<ForbiddenException> { checker.before(exchange) }
  }

  @Test fun `access granted`() {
    every { exchange.session["userId"] } returns admin.id.toString()
    every { exchange.route.annotations } returns listOf(Access(ADMIN))
    checker.before(exchange)
    verify {
      exchange.attr("user", admin)
      userRepository.setAppUser(admin)
    }
  }

  @Test fun `forbids access without matching role`() {
    every { exchange.session["userId"] } returns user.id.toString()
    every { exchange.route.annotations } returns listOf(Access(ADMIN))
    assertThrows<ForbiddenException> { checker.before(exchange) }
    verify { exchange.attr("user", user) }
  }

  @Test fun `Access annotation overrides Public (eg on class)`() {
    every { exchange.session["userId"] } returns user.id.toString()
    every { exchange.route.annotations } returns listOf(Public(), Access(ADMIN))
    assertThrows<ForbiddenException> { checker.before(exchange) }
  }

  @Test fun `allows access for route with multiple roles`() {
    every { exchange.session["userId"] } returns admin.id.toString()
    every { exchange.route.annotations } returns listOf(Access(ADMIN, VIEWER))
    checker.before(exchange)
    verify { exchange.attr("user", admin) }
  }

  @Test fun `requires @Access annotation`() {
    every { exchange.session["userId"] } returns admin.id.toString()
    assertThrows<IllegalStateException> { checker.before(exchange) }
  }
}
