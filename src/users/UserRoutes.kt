package users

import auth.Access
import db.Id
import db.today
import klite.HttpExchange
import klite.MimeTypes
import klite.StatusCode.Companion.OK
import klite.annotations.*
import klite.i18n.lang
import klite.jdbc.BetweenExcl
import klite.jdbc.or
import users.Role.*
import java.time.LocalDate

@Access(ADMIN)
class UserRoutes(
  private val userRepository: UserRepository,
) {
  @GET
  fun all(@QueryParam from: LocalDate, @QueryParam until: LocalDate) =
    userRepository.list(or(User::role to ADMIN, User::lastOnlineAt to null, User::lastOnlineAt to BetweenExcl(from, until.plusDays(1))))

  @POST @Access(ADMIN)
  fun save(user: User): User {
    userRepository.save(user)
    return user
  }

  @PUT("/:id/lang") @Access(ADMIN)
  fun updateLang(@PathParam id: Id<User>, req: ChangeLangRequest): User {
    val user = userRepository.get(id)
    userRepository.save(user.copy(lang = req.lang))
    return user
  }

  @GET("/:id") @Access(ADMIN)
  fun user(@PathParam id: Id<User>) = userRepository.get(id)
}

typealias LangCode = String
data class ChangeLangRequest(val lang: LangCode)
