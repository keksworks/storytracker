package mcp

import auth.ApiKeyRepository
import db.Id
import klite.ForbiddenException
import klite.HttpExchange
import klite.UnauthorizedException
import klite.annotations.GET
import klite.annotations.POST
import klite.jdbc.NoTransaction
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
  private val tools = listOf(
    ToolDef("list_projects", "List all projects you have access to", NoArgs::class),
    ToolDef("list_stories", "List stories in a project (excludes done/accepted stories by default)", ListStoriesArgs::class),
    ToolDef("get_story", "Get full details of a story by ID", GetStoryArgs::class),
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
    val toolName = params["name"] as String
    val args = (params["arguments"] as? Map<String, Any?>) ?: emptyMap()
    val argsJson = jsonMapper.render(args)
    val result = when (toolName) {
      "list_projects" -> listProjects(user)
      "list_stories" -> listStories(user, jsonMapper.parse<ListStoriesArgs>(argsJson))
      "get_story" -> getStory(user, jsonMapper.parse<GetStoryArgs>(argsJson))
      else -> throw IllegalArgumentException("Unknown tool: $toolName")
    }
    val json = jsonMapper.render(result)
    return ToolCallResult(listOf(ToolContent(text = json)))
  }

  private fun listProjects(user: User): List<Project> =
    if (user.isAdmin) projectRepository.list() else projectRepository.listForMember(user.id)

  private fun listStories(user: User, args: ListStoriesArgs): List<Story> {
    requireAccess(user, args.projectId)
    var stories = storyRepository.list(args.projectId, q = args.q)
    if (args.status != null) stories = stories.filter { it.status == args.status }
    else stories = stories.filter { it.status != ACCEPTED }
    if (args.type != null) stories = stories.filter { it.type == args.type }
    return stories
  }

  private fun getStory(user: User, args: GetStoryArgs): Story {
    val story = storyRepository.get(args.storyId)
    requireAccess(user, story.projectId)
    return story
  }

  private fun requireAccess(user: User, projectId: Id<Project>) {
    if (!user.isAdmin) {
      projectMemberRepository.role(projectId, user.id) ?: throw ForbiddenException("Not a member of this project")
    }
  }
}
