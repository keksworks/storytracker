package auth

import email.EmailContent
import klite.Email
import klite.HttpExchange
import klite.NotFoundException
import klite.annotations.POST
import klite.i18n.lang
import klite.jdbc.nowSec
import klite.oauth.initSession
import klite.smtp.EmailSender
import users.User
import users.UserRepository

class EmailAuthRoutes(
  private val userRepository: UserRepository,
  private val emailSender: EmailSender,
) {
  @POST("/email") fun sendCode(req: EmailRequest, e: HttpExchange) =
    sendCode(e, req, "loginCode")

  internal fun sendCode(e: HttpExchange, req: EmailRequest, emailKey: String) {
    val code = (1..6).joinToString("") { (0..9).random().toString() }
    e.session["emailCode"] = "${req.email}:$code"
    e.session["codeTime"] = nowSec().epochSecond.toString()
    emailSender.send(req.email, EmailContent(e.lang, emailKey, mapOf(
      "code" to code,
      "email" to req.email.value
    )))
  }

  @POST("/email/code") fun loginWithCode(req: EmailCodeRequest, e: HttpExchange): User {
    checkCode(e, req)
    val user = userRepository.by(User::email to req.email)
    if (user == null) throw NotFoundException("login.noUser")
    e.initSession(user)
    return user
  }

  internal fun checkCode(e: HttpExchange, req: EmailCodeRequest) {
    require(nowSec().epochSecond - (e.session["codeTime"]?.toLong() ?: 0) < 10 * 60 * 1000) { "Code expired" }
    require(e.session["emailCode"] == "${req.email}:${req.code}") { "Invalid code" }
  }
}

data class EmailRequest(val email: Email)
data class EmailCodeRequest(val email: Email, val code: String)
