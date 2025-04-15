package users

import db.CrudRepository
import db.Id
import db.getJsonOrNull
import db.jsonb
import klite.jdbc.create
import klite.jdbc.exec
import klite.toValues
import java.sql.ResultSet
import javax.sql.DataSource

class UserRepository(db: DataSource): CrudRepository<User>(db, "users") {
  fun setAppUser(user: User) {
    db.exec("call set_app_user(?)", sequenceOf(user.id))
  }

  override fun ResultSet.mapper() = mapper("")
  internal fun ResultSet.mapper(columnPrefix: String) = create<User>(columnPrefix,
    User::address to getJsonOrNull<Address>("address")
  )

  fun list(ids: List<Id<User>>) = if (ids.isEmpty()) emptyList() else
    list(User::id to ids, suffix = "order by firstName, lastName")

  override fun User.persister() = toValues(
    User::address to address?.let { jsonb(it) }
  )
}
