package auth

import ch.tutteli.atrium.api.fluent.en_GB.toContain
import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import db.BaseMocks
import db.TestData.apiKey
import db.TestData.user
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test

class ApiKeyRoutesTest: BaseMocks() {
  val routes = ApiKeyRoutes(apiKeyRepository)

  @Test fun `list returns user keys`() {
    val result = routes.list(user)
    expect(result).toEqual(listOf(apiKey))
    verify { apiKeyRepository.listForUser(user.id) }
  }

  @Test fun `create returns existing key if one exists`() {
    val result = routes.create(user)
    expect(result).toEqual(apiKey)
  }

  @Test fun `create generates new key when none exists`() {
    every { apiKeyRepository.listForUser(user.id) } returns emptyList()
    val result = routes.create(user)
    expect(result.key).toContain("st-")
    expect(result.userId).toEqual(user.id)
    verify { apiKeyRepository.create(any()) }
  }

  @Test fun `delete removes key`() {
    routes.delete(apiKey.id, user)
    verify { apiKeyRepository.deleteForUser(apiKey.id, user.id) }
  }
}
