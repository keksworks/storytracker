package users

import ch.tutteli.atrium.api.fluent.en_GB.toBeEmpty
import ch.tutteli.atrium.api.fluent.en_GB.toContain
import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import db.DBTest
import db.Id
import db.TestData.user
import klite.jdbc.query
import org.junit.jupiter.api.Test

class UserRepositoryTest: DBTest() {
  val repository = UserRepository(db)

  @Test fun `save & load`() {
    repository.save(user)
    expect(repository.get(user.id)).toEqual(user)
    expect(repository.list()).toContain(user)

    expect(repository.list(emptyList())).toBeEmpty()
    expect(repository.list(listOf(Id(), user.id, Id()))).toContain(user)
  }

  @Test fun setAppUser() {
    repository.save(user)
    repository.setAppUser(user)
    expect(db.query("select get_app_user()") { Id<User>(getLong(1)) }.first()).toEqual(user.id)
  }
}
