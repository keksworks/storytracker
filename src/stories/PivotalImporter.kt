package stories

import db.Id
import klite.AppScope
import klite.Config
import klite.Email
import klite.Registry
import klite.info
import klite.jdbc.nowSec
import klite.jdbc.update
import klite.json.*
import klite.logger
import kotlinx.coroutines.launch
import stories.Story.Blocker
import stories.Story.Review
import stories.Story.Status
import stories.Story.Task
import stories.Story.Type
import users.Role
import users.User
import users.UserRepository
import java.net.URI
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.Executors
import kotlin.time.Duration.Companion.seconds

class PivotalImporter(
  registry: Registry,
  private val projectRepository: ProjectRepository,
  private val storyRepository: StoryRepository,
  private val userRepository: UserRepository,
  private val iterationRepository: IterationRepository,
  private val projectMemberRepository: ProjectMemberRepository,
  private val epicRepository: EpicRepository,
  private val attachmentRepository: AttachmentRepository
) {
  private val log = logger()
  private val token = Config["PIVOTAL_API_TOKEN"]
  private val downloadPool = Executors.newFixedThreadPool(10)
  private val http = JsonHttpClient("https://www.pivotaltracker.com/services/v5", reqModifier = {
    setHeader("X-TrackerToken", token)
  }, registry = registry, retryCount = 2, retryAfter = 30.seconds)

  suspend fun importProjects() {
    var num = 0
    http.get<JsonList>("/projects").forEach { p ->
      val name = p.getString("name")
      log.info("Importing project $name")
      val project = Project(Id(p.getLong("id")), name, p.getStringOrNull("description"),
        DayOfWeek.valueOf(p.getString("week_start_day").uppercase()),
        p.getOrNull("iteration_weeks") ?: 1, p.getBoolean("bugs_and_chores_are_estimatable"),
        p.getNode("time_zone").getString("olson_name"),
        p.getInt("velocity_averaged_over"), reviewTypes = emptySet(),
        p.getInt("version"), p.getInt("current_iteration_number"),
        Instant.parse(p.getString("updated_at")), Instant.parse(p.getString("created_at")))
      projectRepository.save(project)
      num++
    }
    log.info("Imported $num projects")
  }

  suspend fun importStories(project: Project, downloadAttachments: Boolean = false) {
    var num = 0
    var afterId: Id<Story>? = null
    val reviewTypes = mutableSetOf<String>()
    val lastUpdated = storyRepository.lastUpdated(project.id)
    while (num % 500 == 0) {
      val fields = listOf("name", "description", "current_state", "story_type", "estimate", "labels", "comments(:default,file_attachments)", "reviews(:default,review_type)", "tasks", "blockers", "accepted_at", "updated_at", "created_at", "requested_by_id")
      http.get<JsonList>("/projects/${project.id}/stories?limit=500&offset=$num&fields=" + fields.joinToString(",") + (lastUpdated?.let { "&updated_after=$it" } ?: "")).forEach { p ->
        val id = Id<Story>(p.getLong("id"))
        val name = p.getString("name")
        log.info("Importing story ${id.value} $name")
        val story = Story(
          id, project.id, name, p.getStringOrNull("description"),
          Type.valueOf(p.getString("story_type").uppercase()),
          Status.valueOf(p.getString("current_state").uppercase()),
          afterId = afterId,
          points = p.getOrNull("estimate"),
          tags = p.getList<JsonNode>("labels").map { it.getString("name") }.toSet(),
          comments = getComments(p.getList("comments"), project.id, id, downloadAttachments),
          tasks = p.getList<JsonNode>("tasks").map {
            val completed = it.getBoolean("complete")
            Task(it.getString("description"), if (completed) it.getStringOrNull("updated_at")?.let { Instant.parse(it) } else null,
              Instant.parse(it.getString("created_at")))
          },
          blockers = p.getList<JsonNode>("blockers").map {
            val resolved = it.getBoolean("resolved")
            Blocker(it.getString("description"), Id(it.getLong("person_id")), if (resolved) it.getStringOrNull("updated_at")?.let { Instant.parse(it) } else null,
              Instant.parse(it.getString("created_at")))
          },
          reviews = p.getList<JsonNode>("reviews").map {
            Review(it.getNode("review_type").getString("name"), Review.Status.valueOf(it.getString("status").uppercase()),
              it.getOrNull<Number>("reviewer_id")?.let { Id(it.toLong()) },
              Instant.parse(it.getString("created_at")), Instant.parse(it.getString("updated_at")))
          },
          acceptedAt = p.getStringOrNull("accepted_at")?.let { Instant.parse(it) },
          updatedAt = Instant.parse(p.getString("updated_at")),
          createdAt = Instant.parse(p.getString("created_at")),
          createdBy = Id(p.getLong("requested_by_id")),
        )
        storyRepository.save(story)
        reviewTypes.addAll(story.reviews.map { it.type })
        num++
        afterId = id
      }
      log.info("Imported $num stories")
      if (num == 0) break
    }

    if (!project.reviewTypes.containsAll(reviewTypes)) {
      val updatedReviewTypes = project.reviewTypes + reviewTypes
      projectRepository.save(project.copy(reviewTypes = updatedReviewTypes))
      log.info("Updated review types for project ${project.id} to $updatedReviewTypes")
    }
  }

  suspend fun importIterations(project: Project) {
    var numStories = 0
    val lastIteration = iterationRepository.list(project.id, "order by number desc limit 1").firstOrNull()?.number
    var num = lastIteration ?: 0
    while (num < project.iterations) {
      http.get<JsonList>("/projects/${project.id}/iterations?limit=50&offset=$num&fields=number,length,team_strength,story_ids,length,start,finish,points,accepted_points,velocity").forEach { p ->
        val iteration = Iteration(
          project.id, p.getInt("number"), p.getInt("length"), ((p.get("team_strength") as Number).toDouble() * 100.0).toInt(),
          LocalDate.parse(p.getString("start").substringBefore("T")), p.getStringOrNull("finish")?.let { LocalDate.parse(it.substringBefore("T")) },
          p.getOrNull("points"), p.getOrNull("accepted_points"), p.getInt("velocity"))
        iterationRepository.save(iteration)
        val storyIds = p.getList<Int>("story_ids")
        numStories += storyRepository.setIteration(iteration, storyIds)
        num++
      }
    }
    log.info("Imported $num iterations, updated $numStories stories")
  }

  private fun getComments(nodes: List<JsonNode>, projectId: Id<Project>, ownerId: Id<out Any>, downloadAttachments: Boolean): List<Story.Comment> = nodes.map {
    val attachments = it.getList<JsonNode>("file_attachments").map {
      // Only thumbnailable attachmnets can be downloaded directly because of a bug in Pivotal API, otherwise download_url is used, but requires Pivotal session cookie
      val url = if (it.getBoolean("thumbnailable")) URI(it.getString("big_url"))
      else URI("https://www.pivotaltracker.com" + it.getString("download_url"))
      Story.Attachment(it.getString("filename"), it.getInt("size"), it.getOrNull("width"), it.getOrNull("height"), it.getLong("id")).also {
        if (downloadAttachments) downloadPool.execute { attachmentRepository.download(projectId, ownerId, it, url) }
      }
    }
    Story.Comment(
      it.getStringOrNull("text"), attachments, Id(it.getLong("person_id")),
      Instant.parse(it.getString("updated_at")), Instant.parse(it.getString("created_at"))
    )
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

  suspend fun importEpics(projectId: Id<Project>, downloadAttachments: Boolean = false) {
    var num = 0
    val fields = listOf(":default", "comment_ids")
    http.get<JsonList>("/projects/$projectId/epics?fields=" + fields.joinToString(",")).forEach { p ->
      val name = p.getString("name")
      log.info("Importing epic $name")
      val id = Id<Epic>(p.getLong("id"))
      val commentIds = p.getList<Int>("comment_ids")
      val commentsNodes = if (commentIds.isNotEmpty()) http.get<JsonList>("/projects/$projectId/epics/$id/comments?fields=:default,file_attachments") else emptyList()
      val epic = Epic(
        id, projectId, name, p.getStringOrNull("description"),
        p.getNode("label").getString("name"),
        comments = getComments(commentsNodes, projectId, id, downloadAttachments),
        updatedAt = Instant.parse(p.getString("updated_at")), createdAt = Instant.parse(p.getString("created_at")))
      epicRepository.save(epic)
      num++
    }
    log.info("Imported $num epics")
  }

  private fun JsonNode.getString(key: String) = getStringOrNull(key)!!
  private fun JsonNode.getStringOrNull(key: String) = getOrNull<String>(key)?.replace("\u0000", "")
}
