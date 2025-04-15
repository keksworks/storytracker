package auth

import db.Id
import klite.Before
import klite.ForbiddenException
import klite.HttpExchange
import klite.NotFoundRoute
import klite.RequestMethod.OPTIONS
import users.Role
import users.UserRepository
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

@Target(FUNCTION, CLASS) annotation class Public
@Target(FUNCTION, CLASS) annotation class Access(vararg val roles: Role)

class AccessChecker(private val userRepository: UserRepository): Before {
  override suspend fun before(exchange: HttpExchange) {
    if (exchange.method == OPTIONS) return
    val user = exchange.session["userId"]?.let { userRepository.get(Id(it)) }
    exchange.attr("user", user)
    val access = exchange.route.findAnnotation<Access>()
    val isPublic = access == null && exchange.route.hasAnnotation<Public>() || exchange.route is NotFoundRoute
    if (user == null && !isPublic) throw ForbiddenException()
    if (user != null && !isPublic) {
      if (access == null) error("@Access annotation is required for non-@Public routes")
      if (access.roles.none { it == user.role }) throw ForbiddenException()
    }
    if (user != null) userRepository.setAppUser(user)
  }
}
