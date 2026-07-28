package mcp

import auth.ApiKeyRepository
import db.Id
import klite.ForbiddenException
import klite.HttpExchange
import klite.UnauthorizedException
import klite.annotations.GET
import klite.annotations.POST
import klite.jdbc.NoTransaction
import klite.json.JsonMapper
import klite.nodes.text
import klite.sse.Event
import klite.sse.send
import klite.sse.startEventStream
import stories.*
import stories.Story.Status.ACCEPTED
import users.User
import users.UserRepository

@NoTransaction
class McpRoutes(
  private val apiKeyRepository: ApiKeyRepository,
  private val userRepository: UserRepository,
  private val projectRepository: ProjectRepository,
  private val storyRepository: StoryRepository,
  private val epicRepository: EpicRepository,
  private val projectMemberRepository: ProjectMemberRepository,
) {
  private val jsonMapper = JsonMapper()

  private val tools = listOf(
    ::listProjects to "List all projects you have access to",
    ::listStories to "List user stories in a project (excludes done/accepted stories by default), in order of priority",
    ::getStory to "Get full details of a user story by ID",
    ::listEpics to "Get all epics in a project by ID. Stories belong to epics via matching tags",
  )

  @GET fun sse(e: HttpExchange) {
    authenticate(e) ?: throw UnauthorizedException()
    e.startEventStream()
    e.send(Event("/mcp/rpc", "endpoint"))
    while (true) { Thread.sleep(30_000); e.send(Event("", "ping")) }
  }

  @POST("/rpc") fun rpc(e: HttpExchange, request: JsonRpcRequest): JsonRpcResponse {
    val user = authenticate(e) ?: throw UnauthorizedException()
    if (request.method == "notifications/initialized") return JsonRpcResponse(id = request.id)
    return try {
      JsonRpcResponse(id = request.id, result = handleRequest(user, request.method, request.params))
    } catch (e: Exception) {
      JsonRpcResponse(id = request.id, error = JsonRpcError(-32603, e.message ?: "Internal error"))
    }
  }

  private fun authenticate(e: HttpExchange): User? {
    val auth = e.header("Authorization") ?: return null
    val key = auth.removePrefix("Bearer ").trim()
    if (key.isBlank()) return null
    val apiKey = apiKeyRepository.byKey(key) ?: return null
    apiKeyRepository.updateLastUsed(apiKey.id)
    return userRepository.get(apiKey.userId)
  }

  private fun handleRequest(user: User, method: String, params: Map<String, Any?>): Any = when (method) {
    "initialize" -> InitializeResult()
    "tools/list" -> ToolsListResult(tools.map { it.toTool() })
    "tools/call" -> handleToolCall(user, params)
    "resources/list" -> ResourcesListResult()
    else -> throw IllegalArgumentException("Unknown method: $method")
  }

  @Suppress("UNCHECKED_CAST")
  private fun handleToolCall(user: User, params: Map<String, Any?>): ToolCallResult {
    val toolName = params.text("name")
    val args = (params["arguments"] as? Map<String, Any?>) ?: emptyMap()
    val func = tools.firstOrNull { it.first.name == toolName }?.first ?: throw IllegalArgumentException("Unknown tool: $toolName")
    val result = func.callWith(user, args)
    val json = jsonMapper.render(result)
    return ToolCallResult(listOf(ToolContent(text = json)))
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

  private fun requireAccess(user: User, projectId: Id<Project>) {
    if (!user.isAdmin) {
      projectMemberRepository.role(projectId, user.id) ?: throw ForbiddenException("Not a member of this project")
    }
  }
}
