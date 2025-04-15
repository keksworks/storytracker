package stories

import db.CrudRepository
import db.Entity
import db.Id
import db.getJson
import db.jsonb
import klite.jdbc.UpdatableEntity
import klite.jdbc.create
import klite.jdbc.nowSec
import klite.toValues
import stories.Story.Comment
import stories.Story.Status.UNSTARTED
import stories.Story.Task
import stories.Story.Type.FEATURE
import users.User
import java.net.URI
import java.sql.ResultSet
import java.time.Instant
import java.time.LocalDate
import javax.sql.DataSource

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
  val tasks: List<Task> = emptyList(),
  val comments: List<Comment> = emptyList(),
  val blockerIds: List<Id<Story>> = emptyList(),
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

  data class Comment(
    val text: String,
    val attachments: List<Attachment> = emptyList(),
    val createdBy: Id<User>,
    val updatedAt: Instant = nowSec(),
    val createdAt: Instant = nowSec(),
  )

  data class Attachment(
    val filename: String,
    val size: Int,
    val url: URI,
    val thumbnailUrl: URI? = null,
    val width: Int? = null,
    val height: Int? = null,
  )
}

class StoryRepository(db: DataSource): CrudRepository<Story>(db, "stories") {
  override fun Story.persister() = toValues(Story::tasks to jsonb(tasks), Story::comments to jsonb(comments))
  override fun ResultSet.mapper() = create(Story::tasks to getJson<List<Task>>("tasks"), Story::comments to getJson<List<Comment>>("comments"))
}
