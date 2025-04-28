package stories

import db.CrudRepository
import db.Id
import klite.jdbc.select
import klite.jdbc.upsert
import users.User
import javax.sql.DataSource
import kotlin.reflect.KProperty1

class ProjectRepository(db: DataSource): CrudRepository<Project>(db, "projects") {
  override val defaultOrder get() = "order by updatedAt desc"

  fun listForMember(userId: Id<User>): List<Project> =
    db.select("$table join project_members m on $table.id = m.projectId", ProjectMember::userId to userId) { mapper() }

  fun save(project: Project, skipUpdate: Set<KProperty1<Project, *>>) =
    db.upsert(table, project.persister(), skipUpdateFields = skipUpdate.map { it.name }.toSet())
}
