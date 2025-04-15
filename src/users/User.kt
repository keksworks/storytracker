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
  val personalCode: PersonalCode? = null,
  val phone: Phone? = null,
  val avatarUrl: URI? = null,
  val lastOnlineAt: Instant? = null,
  val department: String? = null,
  val address: Address? = null,
  val lang: String = "et",
  override var updatedAt: Instant? = null,
  override val id: Id<User> = Id()
): Entity<User>, OAuthUser, UpdatableEntity {
  val age get() = personalCode?.age
  val fullName get() = "$firstName $lastName"
}

data class Address(
  val county: String? = null,
  val municipality: String? = null,
  val area: String? = null,
  val street: String? = null,
  val building: String? = null,
  val apartment: String? = null,
  val postalCode: String,
  val details: String? = null,
  val countryCode: String = "EE",
  val id: String
)
