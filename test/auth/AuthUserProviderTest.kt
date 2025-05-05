package auth

import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import db.BaseMocks
import db.TestData.user
import io.mockk.every
import io.mockk.verify
import klite.oauth.OAuthTokenResponse
import klite.oauth.UserProfile
import org.junit.jupiter.api.Test
import users.Role.OWNER
import users.User

class AuthUserProviderTest: BaseMocks() {
  val profile = UserProfile("GOOGLE", "123", user.email, user.firstName, user.lastName)
  val tokenResponse = OAuthTokenResponse("token", 3600)

  val provider = AuthUserProvider(userRepository)

  @Test fun `provides existing user`() {
    every { userRepository.by(User::email to user.email) } returns user
    val providedUser = provider.provide(profile, tokenResponse, exchange) as User
    expect(providedUser).toEqual(user.copy(lastLoginAt = providedUser.lastLoginAt))
  }

  @Test fun `creates a new user`() {
    every { userRepository.save(any()) } returns 1
    every { userRepository.by(User::email to user.email) } returns null

    val user = provider.provide(profile, tokenResponse, exchange) as User

    expect(user.email).toEqual(user.email)
    expect(user.role).toEqual(OWNER)
    verify { userRepository.save(user) }
  }
}
