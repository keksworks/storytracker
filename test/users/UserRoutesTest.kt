package users

import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import db.BaseMocks
import db.TestData.user
import db.TestData.viewer
import db.tomorrow
import db.yesterday
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test

class UserRoutesTest: BaseMocks() {
  val routes = create<UserRoutes>()

  @Test fun all() {
    every { userRepository.list(*anyVararg(), any()) } returns listOf(user, viewer)
    expect(routes.all(yesterday, tomorrow)).toEqual(listOf(user, viewer))
  }

  @Test fun save() {
    val newUser = routes.save(user)
    verify { userRepository.save(newUser) }
  }

  @Test fun updateLang() {
    val changeLangRequest = ChangeLangRequest(lang = "en")
    routes.updateLang(user.id, changeLangRequest)

    verify { userRepository.save(user.copy(lang = "en")) }
  }

  @Test fun `get one user by id`() {
    expect(routes.user(viewer.id)).toEqual(viewer)
  }
}
