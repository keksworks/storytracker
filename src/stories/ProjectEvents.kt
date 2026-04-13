package stories

import db.Id
import klite.jdbc.UpdatableEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.concurrent.ConcurrentHashMap

class ProjectEvents {
  data class Update(val entity: UpdatableEntity, val eventType: String, val requesterId: String = "")

  private val flows = ConcurrentHashMap<Id<Project>, MutableSharedFlow<Update>>()

  fun flow(projectId: Id<Project>) = flows.getOrPut(projectId) { MutableSharedFlow(extraBufferCapacity = 10) }

  fun send(projectId: Id<Project>, entity: UpdatableEntity, eventType: String, requesterId: String = "") {
    flows[projectId]?.tryEmit(Update(entity, eventType, requesterId))
  }
}
