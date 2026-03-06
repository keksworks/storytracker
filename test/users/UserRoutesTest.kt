package users

import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import db.BaseMocks
import db.TestData.admin
import db.TestData.user
import db.tomorrow
import db.yesterday
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test

class UserRoutesTest: BaseMocks() {
  val routes = create<UserRoutes>()

  @Test fun all() {
    every { userRepository.list(*anyVararg(), any()) } returns listOf(admin, user)
    expect(routes.all(yesterday, tomorrow)).toEqual(listOf(admin, user))
  }

  @Test fun save() {
    val newUser = routes.save(admin)
    verify { userRepository.save(newUser) }
  }

  @Test fun updateLang() {
    val changeLangRequest = ChangeLangRequest(lang = "en")
    routes.updateLang(admin.id, changeLangRequest)

    verify { userRepository.save(admin.copy(lang = "en")) }
  }

  @Test fun `get one user by id`() {
    expect(routes.user(user.id)).toEqual(user)
  }
}
