package stories

import db.Id
import klite.Config
import klite.Registry
import klite.info
import klite.json.*
import klite.logger
import stories.Story.Status
import stories.Story.Type
import java.net.URI
import java.time.DayOfWeek
import java.time.Instant

class PivotalImporter(
  registry: Registry,
  private val projectRepository: ProjectRepository,
  private val storyRepository: StoryRepository,
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
    while (num % 100 == 0) {
      val fields = listOf("name", "description", "current_state", "story_type", "estimate", "labels", "comments(:default,file_attachments)", "tasks", "accepted_at", "updated_at", "created_at")
      http.get<JsonList>("/projects/${projectId.value}/stories?limit=100&offset=$num&fields=" + fields.joinToString("%2C")).forEach { p ->
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
              Story.Attachment(it.getString("filename"), it.getInt("size"), url, thumbnailUrl, it.getInt("width"), it.getInt("height"))
            }
            Story.Comment(it.getString("text"), attachments, Id(it.getLong("person_id")),
              Instant.parse(it.getString("updated_at")), Instant.parse(it.getString("created_at")))
         },
          tasks = p.getList<JsonNode>("tasks").map {
            val completed = it.getBoolean("complete")
            Story.Task(it.getString("description"), if (completed) it.getOrNull<String>("updated_at")?.let { Instant.parse(it) } else null,
              Instant.parse(it.getString("created_at")))
          },
          acceptedAt = p.getOrNull<String>("accepted_at")?.let { Instant.parse(it) },
          updatedAt = Instant.parse(p.getString("updated_at")),
          createdAt = Instant.parse(p.getString("created_at"))
        )
        storyRepository.save(story)
        num++
        afterId = id
      }
      log.info("Imported $num stories")
    }
  }
}
