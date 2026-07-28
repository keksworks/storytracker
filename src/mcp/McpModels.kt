package mcp

import klite.SnakeCase
import klite.json.JsonMapper
import klite.publicProperties
import kotlin.reflect.KClass
import kotlin.reflect.KType

internal val jsonMapper = JsonMapper(keys = SnakeCase)

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

data class ToolDef<T: Any>(
  val name: String,
  val description: String,
  val inputClass: KClass<T>,
)

fun ToolDef<*>.toTool() = Tool(name, description, inputClass.toToolSchema())

private fun KClass<*>.toToolSchema(): ToolSchema {
  val required = mutableListOf<String>()
  val properties = publicProperties.values.associate { prop ->
    val jsonName = jsonMapper.keys.to(prop.name)
    if (!prop.returnType.isMarkedNullable) required += jsonName
    jsonName to mapOf("type" to prop.returnType.toJsonSchemaType())
  }
  return ToolSchema(properties = properties, required = required)
}

private fun KType.toJsonSchemaType(): String = when (classifier) {
  Long::class, Int::class, Short::class, Byte::class -> "number"
  Double::class, Float::class -> "number"
  Boolean::class -> "boolean"
  else -> "string"
}

internal object NoArgs
