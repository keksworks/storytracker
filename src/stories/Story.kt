package stories

import db.Entity
import db.Id
import klite.jdbc.nowSec
import stories.Story.Status.UNSTARTED
import stories.Story.Type.FEATURE
import users.User
import java.time.Instant
import java.time.LocalDate

data class Story(
  override val id: Id<Story> = Id(),
  val projectId: Id<Project>,
  val name: String,
  val description: String? = null,
  val type: Type = FEATURE,
  val status: Status = UNSTARTED,
  val tags: List<String> = emptyList(),
  val points: Int? = null,
  val externalId: String? = null,
  val iteration: Int? = null,
  val tasks: List<Task> = emptyList(),
  val comments: List<Comment> = emptyList(),
  val blockers: List<Blocker> = emptyList(),
  // TODO reviews, code commits?
  val afterId: Id<Story>? = null,
  val acceptedAt: Instant? = null,
  val deadline: LocalDate? = null,
  val updatedAt: Instant? = nowSec(),
  val createdAt: Instant = nowSec(),
  val createdBy: Id<User>? = null,
): Entity<Story>/*, UpdatableEntity */ {
  enum class Type {
    FEATURE, BUG, CHORE, RELEASE
  }

  enum class Status {
    ACCEPTED, DELIVERED, FINISHED, STARTED, REJECTED, PLANNED, UNSTARTED, UNSCHEDULED
  }

  data class Task(
    val text: String,
    val completedAt: Instant? = null,
    val createdAt: Instant = nowSec(),
  )

  data class Blocker(
    val text: String?,
    val createdBy: Id<User>,
    val resolvedAt: Instant? = null,
    val createdAt: Instant = nowSec(),
  )

  data class Comment(
    val text: String?,
    val attachments: List<Attachment> = emptyList(),
    val createdBy: Id<User>,
    val updatedAt: Instant = nowSec(),
    val createdAt: Instant = nowSec(),
  )

  data class Attachment(
    val filename: String,
    val size: Int,
    val width: Int? = null,
    val height: Int? = null,
    val id: Long? = null,
  )
}
