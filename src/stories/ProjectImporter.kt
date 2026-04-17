package stories

import db.Id
import klite.ForbiddenException
import klite.jdbc.UpdatableEntity
import klite.jdbc.eq
import stories.Project.Status.ACTIVE
import users.Role.ADMIN
import users.Role.OWNER
import users.User
import users.UserRepository
import java.time.Instant.MIN

class ProjectImporter(
  private val projectRepository: ProjectRepository,
  private val iterationRepository: IterationRepository,
  private val projectMemberRepository: ProjectMemberRepository,
  private val storyRepository: StoryRepository,
  private val epicRepository: EpicRepository,
  private val userRepository: UserRepository
) {
  fun import(export: ProjectExport, user: User): Project {
    saveOrCreateProject(export, user)
    val userIdMap = importMembers(export)
    importIterations(export)
    importEpics(export, userIdMap)
    importStories(export, userIdMap)

    return export.project
  }

  private fun saveOrCreateProject(export: ProjectExport, user: User) {
    val existingProject = runCatching { projectRepository.get(export.project.id) }.getOrNull()

    if (existingProject == null) {
      projectRepository.create(export.project.copy(status = ACTIVE))
      projectMemberRepository.save(ProjectMember(export.project.id, user.id, OWNER))
    } else {
      if (export.project.isNewer(existingProject)) projectRepository.save(export.project.copy(status = ACTIVE, updatedAt = existingProject.updatedAt))
      val userRole = if (user.isAdmin) ADMIN else projectMemberRepository.role(existingProject.id, user.id)
      if (userRole !in setOf(ADMIN, OWNER)) throw ForbiddenException("projects.importForbidden")
    }
  }

  private fun UpdatableEntity.isNewer(other: UpdatableEntity) = (updatedAt ?: MIN) > (other.updatedAt ?: MIN)
  private fun Project.isNewer(other: Project) = (this as UpdatableEntity).isNewer(other as UpdatableEntity) ||
                                                currentIterationNum > other.currentIterationNum

  private fun importMembers(export: ProjectExport): Map<Id<User>, Id<User>> {
    val userIdMap = mutableMapOf<Id<User>, Id<User>>()

    val existingMembers = projectMemberRepository.listWithUsers(export.project.id)
    export.memberUsers.forEach { importedMember ->
      val user = userRepository.by(User::email eq importedMember.user.email)?.also { userIdMap[importedMember.user.id] = it.id } ?:
        importedMember.user.copy(isAdmin = false).also { userRepository.create(it) }

      val member = existingMembers.find { it.member.userId == user.id }
      if (member == null) projectMemberRepository.create(importedMember.member.copy(userId = user.id))
    }
    return userIdMap
  }

  // TODO: use batch insert/update for speed
  private fun importIterations(export: ProjectExport) {
    val existingIterations = iterationRepository.list(export.project.id).associateBy { it.number }
    export.iterations.forEach { iteration ->
      if (iteration.number !in existingIterations) iterationRepository.save(iteration)
    }
  }

  private fun importEpics(export: ProjectExport, userIdMap: Map<Id<User>, Id<User>>) {
    val existingEpics = epicRepository.list(export.project.id).associateBy { it.id }
    export.epics.forEach { epic ->
      val existingEpic = existingEpics[epic.id]
      val updatedEpic = epic.copy(createdBy = userIdMap[epic.createdBy] ?: epic.createdBy)
      if (existingEpic == null) epicRepository.create(updatedEpic)
      else if (epic.isNewer(existingEpic)) epicRepository.save(updatedEpic.copy(updatedAt = existingEpics[epic.id]?.updatedAt))
    }
  }

 private fun importStories(export: ProjectExport, userIdMap: Map<Id<User>, Id<User>>) {
    val existingStories = storyRepository.list(export.project.id).associateBy { it.id }
    export.stories.forEach { story ->
      val exitingStory = existingStories[story.id]
      val updatedStory = story.copy(assignedTo = userIdMap[story.assignedTo] ?: story.assignedTo, createdBy = userIdMap[story.createdBy] ?: story.createdBy)
      if (exitingStory == null) storyRepository.create(updatedStory)
      else if (story.isNewer(exitingStory)) storyRepository.save(updatedStory.copy(updatedAt = exitingStory.updatedAt))
    }
  }
}
