package auth

import klite.Config
import klite.HttpExchange
import klite.jdbc.nowSec
import klite.oauth.OAuthTokenResponse
import klite.oauth.OAuthUser
import klite.oauth.OAuthUserProvider
import klite.oauth.UserProfile
import users.Role.OWNER
import users.Role.VIEWER
import users.User
import users.UserRepository

class AuthUserProvider(
  private val userRepository: UserRepository
): OAuthUserProvider {
  private val ownDomain = Config["OWN_DOMAIN"]

  override fun provide(profile: UserProfile, tokenResponse: OAuthTokenResponse, exchange: HttpExchange): OAuthUser {
    var user = userRepository.by(User::email to profile.email)
    if (user == null) {
      val role = if (profile.email.domain == ownDomain) OWNER else VIEWER
      user = User(profile.firstName + " " + profile.lastName, profile.email, role, avatarUrl = profile.avatarUrl,
        initials = profile.firstName[0] + "" + profile.lastName[0], lastLoginAt = nowSec())
      userRepository.save(user)
    } else {
      user = user.copy(lastLoginAt = nowSec())
      userRepository.save(user)
    }
    return user
  }
}
