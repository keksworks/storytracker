package stories

import db.CrudRepository
import db.Entity
import db.Id
import klite.jdbc.UpdatableEntity
import klite.jdbc.nowSec
import stories.ProjectMember.Role.MEMBER
import users.Role
import users.User
import java.time.DayOfWeek
import java.time.Instant
import javax.sql.DataSource

data class ProjectMember(
  override val id: Id<ProjectMember> = Id(),
  val projectId: Id<Project>,
  val userId: Id<User>,
  val role: Role = MEMBER,
  val commentNotifications: Boolean = false,
  val mentionNotifications: Boolean = false,
  val lastViewedAt: Instant? = null,
  val updatedAt: Instant? = nowSec(),
  val createdAt: Instant = nowSec(),
): Entity<ProjectMember>/*, UpdatableEntity*/ {
  enum class Role {
    OWNER, MEMBER, VIEWER
  }
}

class ProjectMemberRepository(db: DataSource): CrudRepository<ProjectMember>(db, "project_members")
