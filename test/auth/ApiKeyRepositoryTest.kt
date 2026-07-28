package auth

import ch.tutteli.atrium.api.fluent.en_GB.notToEqual
import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import db.DBTest
import db.Id
import db.TestData.user
import org.junit.jupiter.api.Test

class ApiKeyRepositoryTest: DBTest() {
  val repo = ApiKeyRepository(db)

  @Test fun `create and find by key`() {
    val key = ApiKey(user.id, "sk-st-abc123")
    repo.create(key)
    expect(key.id).notToEqual(Id(0))
    expect(repo.byKey("sk-st-abc123")?.id).toEqual(key.id)
  }

  @Test fun `list for user`() {
    val key1 = ApiKey(user.id, "sk-st-key1")
    val key2 = ApiKey(user.id, "sk-st-key2")
    repo.create(key1)
    repo.create(key2)
    val result = repo.listForUser(user.id)
    expect(result.size).toEqual(2)
  }

  @Test fun `delete for user`() {
    val key = ApiKey(user.id, "sk-st-to-delete")
    repo.create(key)
    repo.deleteForUser(key.id, user.id)
    expect(repo.byKey("sk-st-to-delete")).toEqual(null)
  }

  @Test fun `update last used`() {
    val key = ApiKey(user.id, "sk-st-used")
    repo.create(key)
    expect(key.lastUsedAt).toEqual(null)
    repo.updateLastUsed(key.id)
    val updated = repo.byKey("sk-st-used")
    expect(updated?.lastUsedAt).notToEqual(null)
  }
}
