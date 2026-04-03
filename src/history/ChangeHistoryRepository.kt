package history

import db.Id
import db.getJson
import klite.jdbc.BaseRepository
import klite.jdbc.create
import klite.jdbc.notNull
import klite.jdbc.select
import stories.Project
import javax.sql.DataSource

class ChangeHistoryRepository(db: DataSource): BaseRepository(db, "change_history") {
  val defaultOrder get() = "order by changedAt desc"

  fun list(projectId: Id<Project>, limit: Int = 100): List<Change> =
    db.select(table, "projectId" to projectId, Change::changedBy to notNull, suffix = "$defaultOrder limit $limit") {
      create(Change::old to getJson<Map<String, String>>("old"), Change::new to getJson<Map<String, String?>>("new"))
    }
}
