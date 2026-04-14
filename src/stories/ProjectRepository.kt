package stories

import db.CrudRepository
import db.Id
import klite.PropValue
import klite.jdbc.neq
import klite.jdbc.select
import klite.jdbc.update
import klite.toValuesSkipping
import stories.Project.Status.DELETED
import users.User
import javax.sql.DataSource

class ProjectRepository(db: DataSource): CrudRepository<Project>(db, "projects") {
  override val defaultOrder get() = "order by $table.updatedAt desc"
  override fun Project.persister() = toValuesSkipping(Project::tags)

  private val notDeleted = Project::status neq DELETED

  override fun list(vararg where: PropValue<Project, *>?, suffix: String): List<Project> =
    db.select(selectFrom, where.filterNotNull() + notDeleted, suffix) { mapper() }

  fun listForMember(userId: Id<User>): List<Project> =
    db.select("$table join project_members m on $table.id = m.projectId", ProjectMember::userId to userId,
      notDeleted, suffix = defaultOrder) { mapper() }

  fun delete(projectId: Id<Project>) =
    db.update(table, mapOf(Project::status to DELETED), Project::id to projectId)
}
