package db

import db.TestData.user
import db.TestData.viewer
import klite.Config
import klite.isTest
import klite.jdbc.DBMigrator
import klite.jdbc.UpdatableEntity
import klite.jdbc.exec
import klite.jdbc.useAppDBUser
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import users.UserRepository
import kotlin.reflect.full.memberProperties

abstract class DBTest: klite.jdbc.DBTest() {
  companion object {
    init {
      Config["DB_URL"] += "_test"
      DBMigrator().apply {
        if (!Config.isTest) error("Should be test context, but is " + Config.active)
        migrate()
      }
      useAppDBUser()
    }
  }

  @BeforeEach override fun startTx() {
    super.startTx()
    db.exec("call set_app_user(${user.id.value})") {}

    val userRepository = UserRepository(db).apply { save(viewer) }
  }

  @AfterEach fun resetTestData() {
    TestData::class.memberProperties.forEach {
      val entity = it.get(TestData)
      if (entity is UpdatableEntity) entity.updatedAt = null
    }
  }
}
