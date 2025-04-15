package stories

import db.Id
import klite.Config
import klite.Registry
import klite.info
import klite.json.JsonHttpClient
import klite.json.JsonList
import klite.json.JsonNode
import klite.json.getBoolean
import klite.json.getInt
import klite.json.getLong
import klite.json.getNode
import klite.json.getOrNull
import klite.json.getString
import klite.logger
import java.time.DayOfWeek
import java.time.Instant

class PivotalImporter(
  registry: Registry,
  private val projectRepository: ProjectRepository,
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
}
