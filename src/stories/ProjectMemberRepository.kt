package stories

import db.CrudRepository
import db.Id
import klite.jdbc.create
import klite.jdbc.getEnum
import klite.jdbc.query
import klite.jdbc.select
import users.Role
import users.User
import javax.sql.DataSource

class ProjectMemberRepository(db: DataSource): CrudRepository<ProjectMember>(db, "project_members") {
  fun role(projectId: Id<Project>, userId: Id<User>): Role? =
    db.query("select role from $table", ProjectMember::projectId to projectId, ProjectMember::userId to userId)
      { getEnum<Role>("role") }.firstOrNull()

  fun listWithUsers(projectId: Id<Project>): List<ProjectMemberUser> =
    db.select("$table join users u on userId = u.id", ProjectMember::projectId to projectId) {
      ProjectMemberUser(create(), create("u."))
    }
}

data class ProjectMemberUser(val member: ProjectMember, val user: User) {
  val id get() = member.id
}
