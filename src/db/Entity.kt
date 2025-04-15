package db

import klite.TSID
import klite.i18n.Lang
import klite.jdbc.BaseCrudRepository
import klite.jdbc.BaseEntity
import klite.jdbc.getLongOrNull
import klite.json.parse
import java.sql.ResultSet
import javax.sql.DataSource

/** Use TSID by default for all ids */
typealias Id<T> = TSID<T>
typealias Entity<T> = BaseEntity<Id<T>>

abstract class CrudRepository<T: Entity<T>>(db: DataSource, table: String): BaseCrudRepository<T, Id<T>>(db, table) {
  override val orderAsc get() = "order by $table.id"
}

fun jsonb(value: Any) = klite.jdbc.jsonb(Lang.jsonMapper.render(value))
inline fun <reified T: Any> ResultSet.getJsonOrNull(column: String): T? = getString(column)?.let { Lang.jsonMapper.parse<T>(it) }
inline fun <reified T: Any> ResultSet.getJson(column: String): T = getJsonOrNull(column) ?: error("$column is null")

inline fun <reified T: Entity<T>> ResultSet.getIdOrNull(column: String): Id<T>? = getLongOrNull(column)?.let { Id(it) }
inline fun <reified T: Entity<T>> ResultSet.getId(column: String): Id<T> = getIdOrNull(column) ?: error("$column is null")
