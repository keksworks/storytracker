package history

import db.Id
import klite.jdbc.BaseRepository
import klite.jdbc.NotIn
import klite.jdbc.notNull
import klite.jdbc.select
import stories.Project
import javax.sql.DataSource

class ChangeHistoryRepository(db: DataSource): BaseRepository(db, "change_history") {
  val defaultOrder get() = "order by changedAt desc"

  fun list(projectId: Id<Project>, limit: Int = 100): List<Change> =
    db.select(table, "projectId" to projectId, Change::changedBy to notNull, Change::column to NotIn("acceptedat", "ord"),
      suffix = "$defaultOrder limit $limit")
}
