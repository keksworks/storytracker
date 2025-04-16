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
      val project = Project(Id(p.getLong("id")), name, p.getOrNull<String>("description"),
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
          id, projectId, name, p.getOrNull<String>("description"),
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
            Story.Comment(it.getOrNull("text"), attachments, Id(it.getLong("person_id")),
              Instant.parse(it.getString("updated_at")), Instant.parse(it.getString("created_at")))
         },
          tasks = p.getList<JsonNode>("tasks").map {
            val completed = it.getBoolean("complete")
            Story.Task(it.getString("description"), if (completed) it.getOrNull<String>("updated_at")?.let { Instant.parse(it) } else null,
              Instant.parse(it.getString("created_at")))
          },
          blockers = p.getList<JsonNode>("blockers").map {
            val resolved = it.getBoolean("resolved")
            Story.Blocker(it.getString("description"), Id(it.getLong("person_id")), if (resolved) it.getOrNull<String>("updated_at")?.let { Instant.parse(it) } else null,
              Instant.parse(it.getString("created_at")))
          },
          acceptedAt = p.getOrNull<String>("accepted_at")?.let { Instant.parse(it) },
          updatedAt = Instant.parse(p.getString("updated_at")),
          createdAt = Instant.parse(p.getString("created_at")),
          createdBy = Id(p.getLong("requested_by_id")),
        )
        storyRepository.save(story)
        num++
        afterId = id
      }
      log.info("Imported $num stories")
    }
  }

  suspend fun importAccountMembers(accountId: Id<Any>) {
    var num = 0
    http.get<JsonList>("/accounts/${accountId.value}/memberships").forEach { m ->
      val person = m.getNode("person")
      val role = if (m.getBoolean("owner")) Role.OWNER else if (m.getBoolean("admin")) Role.ADMIN else Role.VIEWER
      val name = person.getString("name")
      log.info("Importing member $name $role")
      val user = User(name, Email(person.getString("email")), role,
        initials = person.getString("initials"), username = person.getString("username"),
        updatedAt = m.getOrNull<String>("updated_at")?.let { Instant.parse(it) } ?: nowSec(),
        createdAt = m.getOrNull<String>("created_at")?.let { Instant.parse(it) } ?: nowSec(),
        id = Id(person.getLong("id")))
      userRepository.save(user)
      num++
    }
    log.info("Imported $num account members")
  }
}
