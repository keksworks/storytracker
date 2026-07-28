package mcp

import auth.ApiKeyRepository
import db.Id
import klite.ForbiddenException
import klite.HttpExchange
import klite.SnakeCase
import klite.UnauthorizedException
import klite.annotations.GET
import klite.annotations.POST
import klite.jdbc.NoTransaction
import klite.json.JsonMapper
import klite.json.parse
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
  private val jsonMapper = JsonMapper(keys = SnakeCase)

  private val tools = listOf(
    ToolDef("list_projects", "List all projects you have access to", NoArgs::class),
    ToolDef("list_stories", "List stories in a project (excludes done/accepted stories by default)", ListStoriesArgs::class, required = listOf("project_id")),
    ToolDef("get_story", "Get full details of a story by ID", GetStoryArgs::class, required = listOf("project_id", "story_id")),
  )

  @GET fun sse(e: HttpExchange) {
    authenticate(e) ?: throw UnauthorizedException()
    e.startEventStream()
    e.send(Event("/mcp/rpc", "endpoint"))
    while (true) { Thread.sleep(30_000); e.send(Event("", "ping")) }
  }

  @POST("/rpc") fun rpc(e: HttpExchange): JsonRpcResponse {
    val user = authenticate(e) ?: throw UnauthorizedException()
    val request = jsonMapper.parse<JsonRpcRequest>(e.body<String>())

    if (request.method == "notifications/initialized") return JsonRpcResponse(id = request.id)

    return try {
      JsonRpcResponse(id = request.id, result = handleRequest(user.id, request.method, request.params))
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

  private fun handleRequest(userId: Id<User>, method: String, params: Map<String, Any?>): Any = when (method) {
    "initialize" -> InitializeResult()
    "tools/list" -> ToolsListResult(tools.map { it.toTool() })
    "tools/call" -> handleToolCall(userId, params)
    "resources/list" -> ResourcesListResult()
    else -> throw IllegalArgumentException("Unknown method: $method")
  }

  @Suppress("UNCHECKED_CAST")
  private fun handleToolCall(userId: Id<User>, params: Map<String, Any?>): ToolCallResult {
    val toolName = params["name"] as String
    val args = (params["arguments"] as? Map<String, Any?>) ?: emptyMap()
    val argsJson = jsonMapper.render(args)
    val result = when (toolName) {
      "list_projects" -> listProjects(userId)
      "list_stories" -> listStories(userId, jsonMapper.parse<ListStoriesArgs>(argsJson))
      "get_story" -> getStory(userId, jsonMapper.parse<GetStoryArgs>(argsJson))
      else -> throw IllegalArgumentException("Unknown tool: $toolName")
    }
    val json = jsonMapper.render(result)
    return ToolCallResult(listOf(ToolContent(text = json)))
  }

  private fun listProjects(userId: Id<User>): List<Project> {
    val user = userRepository.get(userId)
    val projects = if (user.isAdmin) projectRepository.list() else projectRepository.listForMember(userId)
    return projects.filter { it.status != Project.Status.DELETED }
  }

  private fun listStories(userId: Id<User>, args: ListStoriesArgs): List<Story> {
    val projectId = Id<Project>(args.projectId)
    requireAccess(userId, projectId)
    var stories = storyRepository.list(projectId, q = args.q)
    if (args.status != null) stories = stories.filter { it.status.name == args.status }
    else stories = stories.filter { it.status != ACCEPTED }
    if (args.type != null) stories = stories.filter { it.type.name == args.type }
    return stories
  }

  private fun getStory(userId: Id<User>, args: GetStoryArgs): Story {
    val storyId = Id<Story>(args.storyId)
    val story = storyRepository.get(storyId)
    requireAccess(userId, story.projectId)
    return story
  }

  private fun requireAccess(userId: Id<User>, projectId: Id<Project>) {
    val user = userRepository.get(userId)
    if (!user.isAdmin) {
      projectMemberRepository.role(projectId, userId) ?: throw ForbiddenException("Not a member of this project")
    }
  }
}
