package stories

import db.CrudRepository
import klite.jdbc.upsert
import javax.sql.DataSource
import kotlin.reflect.KProperty1

class ProjectRepository(db: DataSource): CrudRepository<Project>(db, "projects") {
  fun save(project: Project, skipUpdate: Set<KProperty1<Project, *>>) =
    db.upsert(table, project.persister(), skipUpdateFields = skipUpdate.map { it.name }.toSet())
}
