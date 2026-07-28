package auth

import db.Id
import klite.annotations.*
import users.Role.*
import users.User
import java.util.UUID

@Access(ADMIN, OWNER, MEMBER, VIEWER)
class ApiKeyRoutes(private val apiKeyRepository: ApiKeyRepository) {
  @GET fun list(@AttrParam user: User) = apiKeyRepository.listForUser(user.id)

  @POST fun create(@AttrParam user: User): ApiKey {
    val existing = apiKeyRepository.listForUser(user.id).firstOrNull()
    if (existing != null) return existing
    return ApiKey(userId = user.id, key = "sk-st-${UUID.randomUUID()}").also { apiKeyRepository.create(it) }
  }

  @DELETE("/:id") fun delete(@PathParam id: Id<ApiKey>, @AttrParam user: User) {
    apiKeyRepository.deleteForUser(id, user.id)
  }
}
