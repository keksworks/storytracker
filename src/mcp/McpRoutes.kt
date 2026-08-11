package mcp

import auth.ApiKeyRepository
import db.Id
import klite.ForbiddenException
import klite.HttpExchange
import klite.ai.mcp.ServerInfo
import klite.jdbc.NoTransaction
import klite.jdbc.nowSec
import stories.*
import stories.Story.Status.ACCEPTED
import users.User
import users.UserRepository
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure
import klite.ai.mcp.McpRoutes as McpRoutesBase

@NoTransaction
class McpRoutes(
  private val apiKeyRepository: ApiKeyRepository,
  private val userRepository: UserRepository,
  private val projectRepository: ProjectRepository,
  private val storyRepository: StoryRepository,
  private val epicRepository: EpicRepository,
  private val projectMemberRepository: ProjectMemberRepository,
): McpRoutesBase(ServerInfo("StoryTracker")) {
  override val tools = listOf(
    ::listProjects to "List all projects you have access to",
    ::listStories to "List user stories in a project (excludes accepted stories by default), in order of priority",
    ::getStory to "Get full details of a user story by ID",
    ::listEpics to "Get all epics in a project by ID. Stories belong to epics via matching tags",
    ::addStoryComment to "Add a comment to a user story",
    ::changeStoryStatus to "Change the status of a user story. Finished should be set when implementaion is complete. Add #storyId to commit messages.",
  )

  override fun authenticate(exchange: HttpExchange): User? {
    val auth = exchange.header("Authorization") ?: return null
    val key = auth.removePrefix("Bearer ").trim()
    if (key.isBlank()) return null
    val apiKey = apiKeyRepository.byKey(key) ?: return null
    apiKeyRepository.updateLastUsed(apiKey.id)
    return userRepository.get(apiKey.userId)
  }

  override fun convertValue(value: Any, targetType: KType): Any? = when {
    value is Number && targetType.jvmErasure == Id::class -> Id<Any>(value.toLong())
    else -> super.convertValue(value, targetType)
  }

  fun listProjects(user: User): List<Project> =
    if (user.isAdmin) projectRepository.list() else projectRepository.listForMember(user.id)

  fun listStories(user: User, projectId: Id<Project>, status: Story.Status? = null, type: Story.Type? = null, q: String? = null): List<ListedStory> {
    requireAccess(user, projectId)
    var stories = storyRepository.list(projectId, q = q)
    if (status != null) stories = stories.filter { it.status == status }
    else stories = stories.filter { it.status != ACCEPTED }
    if (type != null) stories = stories.filter { it.type == type }
    return stories.map { ListedStory(it.id, it.name, it.type, it.status, it.points, it.tags) }
  }

  data class ListedStory(val id: Id<Story>, val name: String, val type: Story.Type, val status: Story.Status, val points: Int?, val tags: Set<String>)

  fun getStory(user: User, storyId: Id<Story>, projectId: Id<Project>? = null): Story {
    val story = storyRepository.get(storyId)
    require(projectId == null || story.projectId == projectId) { "Story does not belong to the specified project" }
    requireAccess(user, story.projectId)
    return story
  }

  fun listEpics(user: User, projectId: Id<Project>): List<Epic> {
    requireAccess(user, projectId)
    return epicRepository.list(projectId)
  }

  fun addStoryComment(user: User, storyId: Id<Story>, text: String): Story {
    val story = storyRepository.get(storyId)
    requireAccess(user, story.projectId)
    val comment = Story.Comment(text = text, createdBy = user.id)
    val updated = story.copy(comments = story.comments + comment, updatedAt = nowSec())
    storyRepository.save(updated, skipUpdate = setOf(Story::projectId, Story::createdAt))
    return updated
  }

  fun changeStoryStatus(user: User, storyId: Id<Story>, status: Story.Status): Story {
    val story = storyRepository.get(storyId)
    requireAccess(user, story.projectId)
    val updated = story.copy(status = status, updatedAt = nowSec())
    storyRepository.save(updated, skipUpdate = setOf(Story::projectId, Story::createdAt))
    return updated
  }

  private fun requireAccess(user: User, projectId: Id<Project>) {
    if (!user.isAdmin) {
      projectMemberRepository.role(projectId, user.id) ?: throw ForbiddenException("Not a member of this project")
    }
  }
}
