package auth

import klite.Email
import klite.HttpExchange
import klite.annotations.GET
import klite.annotations.QueryParam
import klite.oauth.initSession
import users.User
import users.UserRepository

class FakeAuthRoutes(private val userRepository: UserRepository) {
  @GET("/login/fake") @Public
  fun loginFake(@QueryParam email: Email, @QueryParam redirect: String = "/", e: HttpExchange) {
    val user = userRepository.by(User::email to email) ?: error("No such user")
    e.initSession(user)
    e.redirect(redirect)
  }
}
