package mcp

import klite.json.JsonProperty

data class JsonRpcResponse(val jsonrpc: String = "2.0", val id: Any?, val result: Any? = null, val error: JsonRpcError? = null)

data class JsonRpcError(val code: Int, val message: String)

data class InitializeResult(
  val protocolVersion: String = "2024-11-05",
  val capabilities: Map<String, Any> = mapOf("tools" to emptyMap<String, Any>()),
  val serverInfo: ServerInfo = ServerInfo(),
)

data class ServerInfo(val name: String = "StoryTracker", val version: String = "1.0.0")

data class ToolsListResult(val tools: List<Tool>)

data class Tool(val name: String, val description: String, val inputSchema: ToolSchema)

data class ToolSchema(val type: String = "object", val properties: Map<String, Any>, val required: List<String> = emptyList())

data class ToolCallResult(val content: List<ToolContent>)

data class ToolContent(val type: String = "text", val text: String)

data class ResourcesListResult(val resources: List<Any> = emptyList())

data class JsonRpcRequest(val jsonrpc: String = "2.0", val id: Any?, val method: String, val params: Map<String, Any?> = emptyMap())

data class ListStoriesArgs(@JsonProperty("project_id") val projectId: Long, val status: String? = null, val type: String? = null, val q: String? = null)

data class GetStoryArgs(@JsonProperty("project_id") val projectId: Long, @JsonProperty("story_id") val storyId: Long)
