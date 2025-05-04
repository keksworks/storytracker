package stories

import db.Id
import klite.jdbc.BaseRepository
import klite.jdbc.gte
import klite.jdbc.select
import klite.jdbc.upsert
import klite.toValues
import javax.sql.DataSource

class IterationRepository(db: DataSource): BaseRepository(db, "iterations") {
  fun save(iteration: Iteration) = db.upsert(table, iteration.toValues(), "projectId,number")
  fun list(projectId: Id<Project>, fromNumber: Int? = null, suffix: String = "order by number") =
    db.select<Iteration>(table, Iteration::projectId to projectId, fromNumber?.let { Iteration::number gte it }, suffix = suffix)
}
