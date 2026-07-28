package mcp

import db.Id
import klite.Converter
import users.User
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

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

data class JsonRpcRequest(val jsonrpc: String = "2.0", val id: Any? = null, val method: String, val params: Map<String, Any?> = emptyMap())

fun Pair<KFunction<*>, String>.toTool(): Tool {
  val (f, description) = this
  val params = f.valueParameters.drop(1) // skip User parameter
  val required = mutableListOf<String>()
  val properties = mutableMapOf<String, Any>()
  for (param in params) {
    val name = param.name!!
    if (!param.type.isMarkedNullable) required += name
    properties[name] = mapOf("type" to param.type.toJsonSchemaType())
  }
  return Tool(f.name, description, ToolSchema(properties = properties, required = required))
}

fun KFunction<*>.callWith(user: User, args: Map<String, Any?>): Any? {
  val params = mutableMapOf<KParameter, Any?>()
  params[valueParameters.first()] = user
  for (param in valueParameters.drop(1)) {
    val value = args[param.name] ?: continue
    params[param] = when {
      value is String -> Converter.from(value, param.type)
      value is Number && param.type.jvmErasure == Id::class -> Id<Nothing>(value.toLong())
      else -> value
    }
  }
  return callBy(params)
}

private fun KType.toJsonSchemaType(): String = when (classifier) {
  Long::class, Int::class, Short::class, Byte::class -> "number"
  Double::class, Float::class -> "number"
  Boolean::class -> "boolean"
  else -> "string"
}
