package users

import db.Entity
import db.Id
import klite.Email
import klite.Phone
import klite.jdbc.UpdatableEntity
import klite.oauth.OAuthUser
import java.net.URI
import java.time.Instant

enum class Role {
  ADMIN, VIEWER
}

data class User(
  override val firstName: String,
  override val lastName: String,
  override val email: Email,
  val role: Role,
  val avatarUrl: URI? = null,
  val lastOnlineAt: Instant? = null,
  val lang: String = "en",
  override var updatedAt: Instant? = null,
  override val id: Id<User> = Id()
): Entity<User>, OAuthUser, UpdatableEntity {
  val fullName get() = "$firstName $lastName"
}
