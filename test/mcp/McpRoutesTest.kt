package mcp

import ch.tutteli.atrium.api.fluent.en_GB.notToContain
import ch.tutteli.atrium.api.fluent.en_GB.toContain
import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import db.BaseMocks
import db.TestData.apiKey
import db.TestData.epic
import db.TestData.project
import db.TestData.story
import db.TestData.story2
import db.TestData.user
import io.mockk.every
import klite.UnauthorizedException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import users.Role.MEMBER

class McpRoutesTest: BaseMocks() {
  val routes = McpRoutes(apiKeyRepository, userRepository, projectRepository, storyRepository, epicRepository, projectMemberRepository)

  init {
    every { exchange.header("Authorization") } returns "Bearer ${apiKey.key}"
  }

  @Test fun `initialize returns protocol info`() {
    val resp = routes.rpc(exchange, JsonRpcRequest(method = "initialize"))
    expect(resp.jsonrpc).toEqual("2.0")
    expect(resp.result).toEqual(InitializeResult())
  }

  @Test fun `tools list returns 6 tools`() {
    val resp = routes.rpc(exchange, JsonRpcRequest(id = "1", method = "tools/list"))
    val result = resp.result as ToolsListResult
    expect(result.tools.size).toEqual(6)
    expect(result.tools.map { it.name }).toContain("listProjects")
    expect(result.tools.map { it.name }).toContain("listStories")
    expect(result.tools.map { it.name }).toContain("getStory")
    expect(result.tools.map { it.name }).toContain("listEpics")
    expect(result.tools.map { it.name }).toContain("addStoryComment")
    expect(result.tools.map { it.name }).toContain("changeStoryStatus")
  }

  @Test fun `tools list generates schema from data class`() {
    val resp = routes.rpc(exchange, JsonRpcRequest(id = "1", method = "tools/list"))
    val result = resp.result as ToolsListResult

    val listStories = result.tools.first { it.name == "listStories" }
    expect(listStories.inputSchema.properties.keys).toContain("projectId")
    expect(listStories.inputSchema.properties.keys).toContain("status")
    expect(listStories.inputSchema.required).toContain("projectId")

    val getStory = result.tools.first { it.name == "getStory" }
    expect(getStory.inputSchema.properties.keys).toContain("projectId")
    expect(getStory.inputSchema.properties.keys).toContain("storyId")
    expect(getStory.inputSchema.required).toContain("storyId")
    expect(getStory.inputSchema.required).notToContain("projectId")
  }

  @Test fun `enum parameters include values in schema`() {
    val resp = routes.rpc(exchange, JsonRpcRequest(id = "1", method = "tools/list"))
    val result = resp.result as ToolsListResult

    val listStories = result.tools.first { it.name == "listStories" }
    val statusProp = listStories.inputSchema.properties["status"] as Map<*, *>
    expect(statusProp["enum"] as List<*>).toContain("UNSTARTED")
    expect(statusProp["enum"] as List<*>).toContain("ACCEPTED")

    val typeProp = listStories.inputSchema.properties["type"] as Map<*, *>
    expect(typeProp["enum"] as List<*>).toContain("FEATURE")
    expect(typeProp["enum"] as List<*>).toContain("BUG")
  }

  @Test fun `list projects for admin`() {
    every { userRepository.get(user.id) } returns user.copy(isAdmin = true)
    every { projectRepository.list() } returns listOf(project)
    val resp = routes.rpc(exchange, JsonRpcRequest(id = "1", method = "tools/call", params = mapOf("name" to "listProjects")))
    val result = resp.result as ToolCallResult
    expect(result.content.first().type).toEqual("text")
    expect(result.content.first().text).toContain("\"name\"")
  }

  @Test fun `list stories with filters`() {
    every { projectMemberRepository.role(project.id, user.id) } returns MEMBER
    every { storyRepository.list(project.id, q = any()) } returns listOf(story, story2)
    val resp = routes.rpc(exchange, JsonRpcRequest(id = "1", method = "tools/call", params = mapOf(
      "name" to "listStories",
      "arguments" to mapOf("projectId" to project.id.value, "status" to "UNSTARTED")
    )))
    val result = resp.result as ToolCallResult
    expect(result.content.first().text).toContain("Story 1")
  }

  @Test fun `get story returns full details`() {
    every { projectMemberRepository.role(project.id, user.id) } returns MEMBER
    every { storyRepository.get(story.id) } returns story
    val resp = routes.rpc(exchange, JsonRpcRequest(id = "1", method = "tools/call", params = mapOf(
      "name" to "getStory",
      "arguments" to mapOf("projectId" to project.id.value, "storyId" to story.id.value)
    )))
    val result = resp.result as ToolCallResult
    expect(result.content.first().text).toContain("Story 1")
    expect(result.content.first().text).toContain("\"status\"")
  }

  @Test fun `list epics`() {
    every { projectMemberRepository.role(project.id, user.id) } returns MEMBER
    every { epicRepository.list(project.id) } returns listOf(epic)
    val resp = routes.rpc(exchange, JsonRpcRequest(id = "1", method = "tools/call", params = mapOf(
      "name" to "listEpics",
      "arguments" to mapOf("projectId" to project.id.value)
    )))
    val result = resp.result as ToolCallResult
    expect(result.content.first().text).toContain("Epic 1")
  }

  @Test fun `add story comment`() {
    every { projectMemberRepository.role(project.id, user.id) } returns MEMBER
    every { storyRepository.get(story.id) } returns story
    every { storyRepository.save(any(), any()) } returns 1
    val resp = routes.rpc(exchange, JsonRpcRequest(id = "1", method = "tools/call", params = mapOf(
      "name" to "addStoryComment",
      "arguments" to mapOf("storyId" to story.id.value, "text" to "New comment")
    )))
    val result = resp.result as ToolCallResult
    expect(result.content.first().text).toContain("New comment")
  }

  @Test fun `change story status`() {
    every { projectMemberRepository.role(project.id, user.id) } returns MEMBER
    every { storyRepository.get(story.id) } returns story
    every { storyRepository.save(any(), any()) } returns 1
    val resp = routes.rpc(exchange, JsonRpcRequest(id = "1", method = "tools/call", params = mapOf(
      "name" to "changeStoryStatus",
      "arguments" to mapOf("storyId" to story.id.value, "status" to "STARTED")
    )))
    val result = resp.result as ToolCallResult
    expect(result.content.first().text).toContain("STARTED")
  }

  @Test fun `unauthorized when no bearer token`() {
    every { exchange.header("Authorization") } returns null
    assertThrows<UnauthorizedException> { routes.rpc(exchange, JsonRpcRequest(method = "any")) }
  }

  @Test fun `unauthorized when invalid key`() {
    every { apiKeyRepository.byKey("invalid") } returns null
    every { exchange.header("Authorization") } returns "Bearer invalid"
    assertThrows<UnauthorizedException> { routes.rpc(exchange, JsonRpcRequest(id = "1", method = "tools/call")) }
  }

  @Test fun `notifications initialized returns ok`() {
    val resp = routes.rpc(exchange, JsonRpcRequest(id = "1", method = "notifications/initialized"))
    expect(resp.jsonrpc).toEqual("2.0")
    expect(resp.result).toEqual(null)
  }
}
