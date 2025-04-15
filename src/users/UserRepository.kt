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

  fun list(ids: List<Id<User>>) = if (ids.isEmpty()) emptyList() else
    list(User::id to ids, suffix = "order by firstName, lastName")
}
