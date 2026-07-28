package mcp

import ch.tutteli.atrium.api.fluent.en_GB.toContain
import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import db.BaseMocks
import db.TestData.apiKey
import db.TestData.project
import db.TestData.story
import db.TestData.story2
import db.TestData.user
import io.mockk.every
import io.mockk.mockk
import klite.HttpExchange
import klite.UnauthorizedException
import klite.json.JsonBody
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import users.Role

class McpRoutesTest: BaseMocks() {
  val jsonBody = JsonBody()
  val routes = McpRoutes(apiKeyRepository, userRepository, projectRepository, storyRepository, epicRepository, projectMemberRepository, jsonBody)

  fun rpcBody(method: String, params: Map<String, Any?> = emptyMap(), id: Long = 1) =
    mapOf("jsonrpc" to "2.0", "method" to method, "id" to id, "params" to params)

  fun rpcExchange(method: String, params: Map<String, Any?> = emptyMap(), id: Long = 1): HttpExchange =
    mockk<HttpExchange>(relaxed = true) {
      every { header("Authorization") } returns "Bearer ${apiKey.key}"
      every { body<Map<String, Any?>>() } returns rpcBody(method, params, id)
    }

  @Test fun `initialize returns protocol info`() {
    val result = routes.rpc(rpcExchange("initialize")) as Map<*, *>
    expect(result["jsonrpc"]).toEqual("2.0")
    val res = result["result"] as Map<*, *>
    expect(res["protocolVersion"]).toEqual("2024-11-05")
    val serverInfo = res["serverInfo"] as Map<*, *>
    expect(serverInfo["name"]).toEqual("StoryTracker")
  }

  @Test fun `tools list returns 3 tools`() {
    val result = routes.rpc(rpcExchange("tools/list")) as Map<*, *>
    val res = result["result"] as Map<*, *>
    val tools = res["tools"] as List<*>
    expect(tools.size).toEqual(3)
    val names = tools.map { (it as Map<*, *>)["name"] }
    expect(names).toContain("list_projects")
    expect(names).toContain("list_stories")
    expect(names).toContain("get_story")
  }

  @Test fun `list projects for admin`() {
    every { userRepository.get(user.id) } returns user.copy(isAdmin = true)
    every { projectRepository.list() } returns listOf(project)
    val result = routes.rpc(rpcExchange("tools/call", mapOf("name" to "list_projects"))) as Map<*, *>
    val res = result["result"] as Map<*, *>
    val content = (res["content"] as List<*>).first() as Map<*, *>
    expect(content["type"]).toEqual("text")
    val text = content["text"] as String
    expect(text).toContain("\"name\"")
  }

  @Test fun `list stories with filters`() {
    every { projectMemberRepository.role(project.id, user.id) } returns Role.MEMBER
    every { storyRepository.list(project.id, q = any()) } returns listOf(story, story2)
    val result = routes.rpc(rpcExchange("tools/call", mapOf(
      "name" to "list_stories",
      "arguments" to mapOf("project_id" to project.id.value, "status" to "UNSTARTED")
    ))) as Map<*, *>
    val res = result["result"] as Map<*, *>
    val content = (res["content"] as List<*>).first() as Map<*, *>
    val text = content["text"] as String
    expect(text).toContain("Story 1")
  }

  @Test fun `get story returns full details`() {
    every { projectMemberRepository.role(project.id, user.id) } returns Role.MEMBER
    every { storyRepository.get(story.id) } returns story
    val result = routes.rpc(rpcExchange("tools/call", mapOf(
      "name" to "get_story",
      "arguments" to mapOf("project_id" to project.id.value, "story_id" to story.id.value)
    ))) as Map<*, *>
    val res = result["result"] as Map<*, *>
    val content = (res["content"] as List<*>).first() as Map<*, *>
    val text = content["text"] as String
    expect(text).toContain("Story 1")
    expect(text).toContain("\"status\"")
  }

  @Test fun `unauthorized when no bearer token`() {
    val exchange = mockk<HttpExchange>(relaxed = true) {
      every { header("Authorization") } returns null
    }
    assertThrows<UnauthorizedException> { routes.rpc(exchange) }
  }

  @Test fun `unauthorized when invalid key`() {
    every { apiKeyRepository.byKey("invalid") } returns null
    val exchange = mockk<HttpExchange>(relaxed = true) {
      every { header("Authorization") } returns "Bearer invalid"
      every { body<Map<String, Any?>>() } returns rpcBody("initialize")
    }
    assertThrows<UnauthorizedException> { routes.rpc(exchange) }
  }

  @Test fun `notifications initialized returns ok`() {
    val result = routes.rpc(rpcExchange("notifications/initialized")) as Map<*, *>
    expect(result["jsonrpc"]).toEqual("2.0")
    expect(result.containsKey("result")).toEqual(false)
  }
}
