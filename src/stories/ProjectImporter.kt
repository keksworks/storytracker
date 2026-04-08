package stories

import klite.ForbiddenException
import klite.i18n.Lang
import klite.jdbc.eq
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
    val existingProject = runCatching { projectRepository.get(export.project.id) }.getOrNull()

    if (existingProject != null) {
      val userRole = if (user.isAdmin) ADMIN else projectMemberRepository.role(existingProject.id, user.id)
      if (userRole !in setOf(ADMIN, OWNER)) throw ForbiddenException(Lang.translate(user.lang, "importForbidden"))
    }
    projectRepository.save(export.project)
    if (existingProject == null)
      projectMemberRepository.save(ProjectMember(export.project.id, user.id, OWNER))

    // TODO: use batch insert/update for speed
    val existingIterations = iterationRepository.list(export.project.id).associateBy {  it.number }
    export.iterations.forEach { iteration ->
      if (iteration.number !in existingIterations) iterationRepository.save(iteration)
    }

    val existingEpics = epicRepository.list(Epic::projectId to export.project.id).associateBy { it.id }
    export.epics.forEach { epic ->
      val existingEpic = existingEpics[epic.id]
      if (existingEpic == null) epicRepository.create(epic)
      else if ((epic.updatedAt ?: MIN) > (existingEpic.updatedAt ?: MIN)) {
        epicRepository.save(epic.copy(updatedAt = existingEpics[epic.id]?.updatedAt)) }
    }

    val existingStories = storyRepository.list(export.project.id).associateBy { it.id }
    export.stories.forEach { story ->
      val exitingStory = existingStories[story.id]
      if (exitingStory == null) storyRepository.create(story)
      else if ((story.updatedAt ?: MIN) > (exitingStory.updatedAt ?: MIN)) {
        storyRepository.save(story.copy(updatedAt = exitingStory.updatedAt)) }
    }

    val existingMembers = projectMemberRepository.listWithUsers(export.project.id)
    export.memberUsers.forEach { memberUser ->
      val user = userRepository.by(User::email eq memberUser.user.email) ?:
      memberUser.user.copy(isAdmin = false).also { userRepository.create(it) }

      val member = existingMembers.find { it.member.userId == user.id }
      if (member == null) projectMemberRepository.create(memberUser.member.copy(userId = user.id))
    }
    return export.project
  }
}
