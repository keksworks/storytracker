package auth

import db.Entity
import db.Id
import klite.jdbc.UpdatableEntity
import klite.jdbc.nowSec
import users.User
import java.time.Instant

data class ApiKey(
  val userId: Id<User>,
  val key: String,
  val name: String = "MCP",
  val lastUsedAt: Instant? = null,
  val createdAt: Instant = nowSec(),
  override var updatedAt: Instant? = null,
  override val id: Id<ApiKey> = Id(),
): Entity<ApiKey>, UpdatableEntity
