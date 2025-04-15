package db

import klite.*
import klite.jdbc.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

fun Server.initDB() {
  if (Config.isDev) startDevDB()
  logger().info("Migrating " + Config["DB_URL"])
  use(DBMigrator())
  useAppDBUser()
  use(DBModule(PooledDataSource(timeout = 5.seconds, leakCheckThreshold = 7.minutes)))
}
