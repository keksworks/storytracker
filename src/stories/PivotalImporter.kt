package stories

import db.Id
import klite.Config
import klite.Email
import klite.Registry
import klite.info
import klite.jdbc.nowSec
import klite.json.*
import klite.logger
import stories.Story.Status
import stories.Story.Type
import users.Role
import users.User
import users.UserRepository
import java.net.URI
import java.time.DayOfWeek
import java.time.Instant

class PivotalImporter(
  registry: Registry,
  private val projectRepository: ProjectRepository,
  private val storyRepository: StoryRepository,
  private val userRepository: UserRepository,
  private val projectMemberRepository: ProjectMemberRepository,
) {
  private val log = logger()
  private val token = Config["PIVOTAL_API_TOKEN"]
  private val http = JsonHttpClient("https://www.pivotaltracker.com/services/v5", reqModifier = {
    setHeader("X-TrackerToken", token)
  }, registry = registry)

  suspend fun importProjects() {
    var num = 0
    http.get<JsonList>("/projects").forEach { p ->
      val name = p.getString("name")
      log.info("Importing project $name")
      val project = Project(Id(p.getLong("id")), name, p.getStringOrNull("description"),
        DayOfWeek.valueOf(p.getString("week_start_day").uppercase()),
        p.getOrNull<Int>("iteration_weeks") ?: 1, p.getBoolean("bugs_and_chores_are_estimatable"),
        p.getNode("time_zone").getString("olson_name"),
        p.getInt("velocity_averaged_over"), p.getInt("version"), p.getInt("current_iteration_number"),
        Instant.parse(p.getString("updated_at")), Instant.parse(p.getString("created_at")))
      projectRepository.save(project)
      num++
    }
    log.info("Imported $num projects")
  }

  suspend fun importStories(projectId: Id<Project>) {
    var num = 0
    var afterId: Id<Story>? = null
    while (num % 500 == 0) {
      val fields = listOf("name", "description", "current_state", "story_type", "estimate", "labels", "comments(:default,file_attachments)", "tasks", "blockers", "accepted_at", "updated_at", "created_at", "requested_by_id")
      http.get<JsonList>("/projects/${projectId.value}/stories?limit=500&offset=$num&fields=" + fields.joinToString("%2C")).forEach { p ->
        val id = Id<Story>(p.getLong("id"))
        val name = p.getString("name")
        log.info("Importing story ${id.value} $name")
        val story = Story(
          id, projectId, name, p.getStringOrNull("description"),
          Type.valueOf(p.getString("story_type").uppercase()),
          Status.valueOf(p.getString("current_state").uppercase()),
          afterId = afterId,
          points = p.getOrNull<Int>("estimate"),
          tags = p.getList<JsonNode>("labels").map { it.getString("name") },
          comments = p.getList<JsonNode>("comments").map {
            val attachments = it.getList<JsonNode>("file_attachments").map {
              val url = URI(it.getString("big_url"))
              val thumbnailUrl = URI(it.getString("thumbnail_url"))
              Story.Attachment(it.getString("filename"), it.getInt("size"), url, thumbnailUrl, it.getOrNull("width"), it.getOrNull("height"))
            }
            Story.Comment(it.getStringOrNull("text"), attachments, Id(it.getLong("person_id")),
              Instant.parse(it.getString("updated_at")), Instant.parse(it.getString("created_at")))
         },
          tasks = p.getList<JsonNode>("tasks").map {
            val completed = it.getBoolean("complete")
            Story.Task(it.getString("description"), if (completed) it.getStringOrNull("updated_at")?.let { Instant.parse(it) } else null,
              Instant.parse(it.getString("created_at")))
          },
          blockers = p.getList<JsonNode>("blockers").map {
            val resolved = it.getBoolean("resolved")
            Story.Blocker(it.getString("description"), Id(it.getLong("person_id")), if (resolved) it.getStringOrNull("updated_at")?.let { Instant.parse(it) } else null,
              Instant.parse(it.getString("created_at")))
          },
          acceptedAt = p.getStringOrNull("accepted_at")?.let { Instant.parse(it) },
          updatedAt = Instant.parse(p.getString("updated_at")),
          createdAt = Instant.parse(p.getString("created_at")),
          createdBy = Id(p.getLong("requested_by_id")),
        )
        storyRepository.save(story)
        num++
        afterId = id
      }
      log.info("Imported $num stories")
      if (num == 0) break
    }
  }

  suspend fun importAccountMembers(accountId: Id<Any>) {
    var num = 0
    http.get<JsonList>("/accounts/${accountId.value}/memberships").forEach { m ->
      val role = if (m.getBoolean("owner")) Role.OWNER else if (m.getBoolean("admin")) Role.ADMIN else Role.VIEWER
      ensureUserExists(m, role)
      num++
    }
    log.info("Imported $num account members")
  }

  private fun PivotalImporter.ensureUserExists(m: JsonNode, role: Role): Id<User>? {
    val person = m.getNode("person")
    val name = person.getString("name")
    log.info("Importing user $name $role")
    val id = Id<User>(person.getLong("id"))
    if (userRepository.by(User::id to id) != null) return id
    val email = person.getStringOrNull("email")?.let { Email(it) } ?: return null
    val user = User(
      name, email, role,
      initials = person.getString("initials"), username = person.getString("username"),
      updatedAt = m.getStringOrNull("updated_at")?.let { Instant.parse(it) } ?: nowSec(),
      createdAt = m.getStringOrNull("created_at")?.let { Instant.parse(it) } ?: nowSec(),
      id = id)
    userRepository.save(user)
    return id;
  }

  suspend fun importProjectMembers(projectId: Id<Project>) {
    var num = 0
    http.get<JsonList>("/projects/${projectId.value}/memberships").forEach { m ->
      val userId = ensureUserExists(m, Role.VIEWER) ?: return@forEach
      log.info("Importing project member ${userId.value}")
      val member = ProjectMember(Id(m.getLong("id")), projectId, userId,
        role = ProjectMember.Role.valueOf(m.getString("role").uppercase()),
        commentNotifications = m.getBoolean("wants_comment_notification_emails"),
        mentionNotifications = m.getBoolean("will_receive_mention_notifications_or_emails"),
        lastViewedAt = m.getStringOrNull("last_viewed_at")?.let { Instant.parse(it) },
        updatedAt = m.getStringOrNull("updated_at")?.let { Instant.parse(it) } ?: nowSec(),
        createdAt = m.getStringOrNull("created_at")?.let { Instant.parse(it) } ?: nowSec())
      num++
      projectMemberRepository.save(member)
    }
    log.info("Imported $num project members")
  }

  private fun JsonNode.getString(key: String) = getStringOrNull(key)!!
  private fun JsonNode.getStringOrNull(key: String) = getOrNull<String>(key)?.replace("\u0000", "")
}
