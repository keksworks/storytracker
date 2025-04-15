package auth

import db.BaseMocks
import db.TestData.viewer
import db.TestData.user
import io.mockk.every
import io.mockk.verify
import klite.ForbiddenException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import users.Role.*

class AccessCheckerTest: BaseMocks() {
  val checker = create<AccessChecker>()

  @Test fun `allows public access`() = runTest {
    every { exchange.session["userId"] } returns null
    every { exchange.route.annotations } returns listOf(Public())
    checker.before(exchange)
  }

  @Test fun `forbids unauthorized access`() = runTest {
    every { exchange.session["userId"] } returns null
    assertThrows<ForbiddenException> { checker.before(exchange) }
  }

  @Test fun `access granted`() = runTest {
    every { exchange.session["userId"] } returns user.id.toString()
    every { exchange.route.annotations } returns listOf(Access(user.role))
    checker.before(exchange)
    verify {
      exchange.attr("user", user)
      userRepository.setAppUser(user)
    }
  }

  @Test fun `forbids access without matching role`() = runTest {
    every { exchange.session["userId"] } returns viewer.id.toString()
    every { exchange.route.annotations } returns listOf(Access(VIEWER))
    assertThrows<ForbiddenException> { checker.before(exchange) }
    verify { exchange.attr("user", viewer) }
  }

  @Test fun `Access annotation overrides Public (eg on class)`() = runTest {
    every { exchange.session["userId"] } returns viewer.id.toString()
    every { exchange.route.annotations } returns listOf(Public(), Access(ADMIN))
    assertThrows<ForbiddenException> { checker.before(exchange) }
  }

  @Test fun `allows access for route with multiple roles`() = runTest {
    every { exchange.session["userId"] } returns user.id.toString()
    every { exchange.route.annotations } returns listOf(Access(ADMIN, VIEWER))
    checker.before(exchange)
    verify { exchange.attr("user", user) }
  }

  @Test fun `requires @Access annotation`() = runTest {
    every { exchange.session["userId"] } returns user.id.toString()
    assertThrows<IllegalStateException> { checker.before(exchange) }
  }
}
