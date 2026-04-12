package stories

import db.Id
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.concurrent.ConcurrentHashMap

class StoryEvents {
  private val flows = ConcurrentHashMap<Id<Project>, MutableSharedFlow<Pair<Story, String>>>()
  private val epicFlows = ConcurrentHashMap<Id<Project>, MutableSharedFlow<Pair<Epic, String>>>()

  fun flow(projectId: Id<Project>) = flows.getOrPut(projectId) { MutableSharedFlow(extraBufferCapacity = 10) }
  fun epicFlow(projectId: Id<Project>) = epicFlows.getOrPut(projectId) { MutableSharedFlow(extraBufferCapacity = 10) }

  fun sendUpdates(projectId: Id<Project>, story: Story, requesterId: String = "") {
    flows[projectId]?.tryEmit(story to requesterId)
  }

  fun sendEpicUpdates(projectId: Id<Project>, epic: Epic, requesterId: String = "") {
    epicFlows[projectId]?.tryEmit(epic to requesterId)
  }
}
