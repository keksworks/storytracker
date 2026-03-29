package db

import klite.Email
import stories.*
import users.Role
import users.User
import java.time.LocalDate
import java.time.ZoneOffset.UTC

/** Immutable domain object samples for unit tests */
object TestData {
  val date = LocalDate.of(2025, 3, 3)
  val now = date.atStartOfDay().toInstant(UTC)

  val admin = User("Test Admin", Email("admin@azib.net"), isAdmin = true, createdAt = now)
  val user = User("Test User", Email("user@azib.net"), createdAt = now)

  val project = Project(Id(1), "Project 1", "Description", createdAt = now)
  val iteration = Iteration(project.id, 1, startDate = date, endDate = date.plusDays(7))
  val epic = Epic(Id(), project.id, "Epic 1", tag = "epic1", createdAt = now)
  val story = Story(Id(), project.id, "Story 1", createdAt = now)
  val story2 = Story(Id(), project.id, "Story 2", order = 2.0, createdAt = now)
  val projectMember = ProjectMember(project.id, user.id, Role.MEMBER)
  val projectMemberUser = ProjectMemberUser(projectMember, user)
}
