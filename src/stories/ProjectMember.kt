package stories

import db.Entity
import db.Id
import klite.jdbc.nowSec
import users.Role
import users.Role.MEMBER
import users.User
import java.time.Instant

data class ProjectMember(
  val projectId: Id<Project>,
  val userId: Id<User>,
  val role: Role = MEMBER,
  val commentNotifications: Boolean = false,
  val mentionNotifications: Boolean = false,
  val lastViewedAt: Instant? = null,
  val updatedAt: Instant? = nowSec(),
  val createdAt: Instant = nowSec(),
  override val id: Id<ProjectMember> = Id(),
): Entity<ProjectMember>/*, UpdatableEntity*/
