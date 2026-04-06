package stories

import db.CrudRepository
import db.Id
import klite.jdbc.select
import klite.toValuesSkipping
import users.User
import javax.sql.DataSource

class ProjectRepository(db: DataSource): CrudRepository<Project>(db, "projects") {
  override val defaultOrder get() = "order by $table.updatedAt desc"
  override fun Project.persister() = toValuesSkipping(Project::tags)

  fun listForMember(userId: Id<User>): List<Project> =
    db.select("$table join project_members m on $table.id = m.projectId", ProjectMember::userId to userId, suffix = defaultOrder) { mapper() }
}
