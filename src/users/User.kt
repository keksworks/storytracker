package users

import db.Entity
import db.Id
import klite.Email
import klite.Phone
import klite.jdbc.UpdatableEntity
import klite.jdbc.nowSec
import klite.oauth.OAuthUser
import java.net.URI
import java.time.Instant

enum class Role {
  OWNER, ADMIN, VIEWER
}

data class User(
  val name: String,
  override val email: Email,
  val role: Role,
  val avatarUrl: URI? = null,
  val initials: String? = null,
  val username: String? = null,
  val lastOnlineAt: Instant? = null,
  val lang: String = "en",
  val updatedAt: Instant? = nowSec(),
  val createdAt: Instant = nowSec(),
  override val id: Id<User> = Id(),
): Entity<User>, OAuthUser/*, UpdatableEntity*/ {
  override val firstName get() = name.substringBefore(" ")
  override val lastName get() = name.substringAfter(" ")
}
