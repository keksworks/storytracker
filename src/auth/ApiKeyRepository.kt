package auth

import db.CrudRepository
import db.Id
import klite.jdbc.delete
import klite.jdbc.eq
import klite.jdbc.update
import users.User
import java.time.Instant
import javax.sql.DataSource

class ApiKeyRepository(db: DataSource): CrudRepository<ApiKey>(db, "api_keys") {
  fun byKey(key: String) = by(ApiKey::key eq key)

  fun listForUser(userId: Id<User>) = list(ApiKey::userId to userId, suffix = "order by createdAt")

  fun deleteForUser(id: Id<ApiKey>, userId: Id<User>) =
    db.delete(table, ApiKey::id to id, ApiKey::userId to userId)

  fun updateLastUsed(id: Id<ApiKey>) =
    db.update(table, mapOf(ApiKey::lastUsedAt to Instant.now()), ApiKey::id to id)
}
