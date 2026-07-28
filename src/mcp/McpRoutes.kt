package mcp

import auth.ApiKeyRepository
import db.Id
import klite.ForbiddenException
import klite.HttpExchange
import klite.UnauthorizedException
import klite.annotations.GET
import klite.annotations.POST
import klite.jdbc.NoTransaction
import klite.json.JsonBody
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
  private val jsonBody: JsonBody,
) {
  @GET fun sse(e: HttpExchange) {
    authenticate(e) ?: throw UnauthorizedException()
    e.startEventStream()
    e.send(Event("/mcp/rpc", "endpoint"))
    while (true) { Thread.sleep(30_000); e.send(Event("", "ping")) }
  }

  @POST("/rpc") fun rpc(e: HttpExchange): Any {
    val user = authenticate(e) ?: throw UnauthorizedException()
    val body = e.body<Map<String, Any?>>()
    val id = body["id"]
    val method = body["method"] as String
    val params = (body["params"] as? Map<String, Any?>) ?: emptyMap()

    if (method == "notifications/initialized") return mapOf("jsonrpc" to "2.0", "id" to id)

    return try {
      val result = handleRequest(user.id, method, params)
      mapOf("jsonrpc" to "2.0", "id" to id, "result" to result)
    } catch (e: Exception) {
      jsonRpcError(id, -32603, e.message ?: "Internal error")
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
    "initialize" -> mapOf(
      "protocolVersion" to "2024-11-05",
      "capabilities" to mapOf("tools" to emptyMap<String, Any>()),
      "serverInfo" to mapOf("name" to "StoryTracker", "version" to "1.0.0")
    )
    "tools/list" -> mapOf("tools" to listOf(
      toolDef("list_projects", "List all projects you have access to", emptyMap()),
      toolDef("list_stories", "List stories in a project (excludes done/accepted stories by default)", mapOf(
        "project_id" to mapOf("type" to "number", "description" to "Project ID"),
        "status" to mapOf("type" to "string", "description" to "Filter by status: ${Story.Status.entries.joinToString()}"),
        "type" to mapOf("type" to "string", "description" to "Filter by type: ${Story.Type.entries.joinToString()}"),
        "q" to mapOf("type" to "string", "description" to "Search in story name, description, tags, comments"),
      ), required = listOf("project_id")),
      toolDef("get_story", "Get full details of a story by ID", mapOf(
        "project_id" to mapOf("type" to "number", "description" to "Project ID"),
        "story_id" to mapOf("type" to "number", "description" to "Story ID"),
      ), required = listOf("project_id", "story_id")),
    ))
    "tools/call" -> handleToolCall(userId, params)
    "resources/list" -> mapOf("resources" to emptyList<Any>())
    else -> throw IllegalArgumentException("Unknown method: $method")
  }

  @Suppress("UNCHECKED_CAST")
  private fun handleToolCall(userId: Id<User>, params: Map<String, Any?>): Any {
    val toolName = params["name"] as String
    val args = (params["arguments"] as? Map<String, Any?>) ?: emptyMap()
    val result = when (toolName) {
      "list_projects" -> listProjects(userId)
      "list_stories" -> listStories(userId, args)
      "get_story" -> getStory(userId, args)
      else -> throw IllegalArgumentException("Unknown tool: $toolName")
    }
    val json = jsonBody.json.render(result)
    return mapOf("content" to listOf(mapOf("type" to "text", "text" to json)))
  }

  private fun listProjects(userId: Id<User>): List<Project> {
    val user = userRepository.get(userId)
    val projects = if (user.isAdmin) projectRepository.list() else projectRepository.listForMember(userId)
    return projects.filter { it.status != Project.Status.DELETED }
  }

  private fun listStories(userId: Id<User>, args: Map<String, Any?>): List<Story> {
    val projectId = Id<Project>((args["project_id"] as Number).toLong())
    requireAccess(userId, projectId)
    val status = args["status"] as? String
    val type = args["type"] as? String
    val q = args["q"] as? String
    var stories = storyRepository.list(projectId, q = q)
    if (status != null) stories = stories.filter { it.status.name == status }
    else stories = stories.filter { it.status != ACCEPTED }
    if (type != null) stories = stories.filter { it.type.name == type }
    return stories
  }

  private fun getStory(userId: Id<User>, args: Map<String, Any?>): Story {
    val storyId = Id<Story>((args["story_id"] as Number).toLong())
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

  private fun toolDef(name: String, description: String, properties: Map<String, Any>, required: List<String> = emptyList()) =
    mapOf("name" to name, "description" to description,
      "inputSchema" to mapOf("type" to "object", "properties" to properties, "required" to required))

  private fun jsonRpcError(id: Any?, code: Int, message: String) =
    mapOf("jsonrpc" to "2.0", "id" to id, "error" to mapOf("code" to code, "message" to message))
}
