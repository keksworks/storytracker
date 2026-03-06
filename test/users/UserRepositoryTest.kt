package users

import ch.tutteli.atrium.api.fluent.en_GB.toBeEmpty
import ch.tutteli.atrium.api.fluent.en_GB.toContain
import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import db.DBTest
import db.Id
import db.TestData.admin
import klite.jdbc.query
import org.junit.jupiter.api.Test

class UserRepositoryTest: DBTest() {
  val repository = UserRepository(db)

  @Test fun `save & load`() {
    repository.save(admin)
    expect(repository.get(admin.id)).toEqual(admin)
    expect(repository.list()).toContain(admin)

    expect(repository.list(emptyList())).toBeEmpty()
    expect(repository.list(listOf(Id(), admin.id, Id()))).toContain(admin)
  }

  @Test fun setAppUser() {
    repository.save(admin)
    repository.setAppUser(admin)
    expect(db.query("select get_app_user()") { Id<User>(getLong(1)) }.first()).toEqual(admin.id)
  }
}
