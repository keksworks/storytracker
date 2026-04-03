package db

import db.TestData.admin
import db.TestData.change
import db.TestData.epic
import db.TestData.iteration
import db.TestData.project
import db.TestData.story
import db.TestData.story2
import db.TestData.user
import history.ChangeHistoryRepository
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import klite.Config
import klite.DependencyInjectingRegistry
import klite.HttpExchange
import klite.StatusCode.Companion.Found
import klite.StatusCodeException
import stories.*
import users.UserRepository
import java.net.URI

abstract class BaseMocks {
  companion object {
    init {
      Config.useEnvFile()
    }

    val registry = DependencyInjectingRegistry()
    val exchange = mockk<HttpExchange>(relaxed = true)

    val userRepository = mock<UserRepository>(relaxed = true)
    val projectRepository = mock<ProjectRepository>(relaxed = true)
    val projectMemberRepository = mock<ProjectMemberRepository>(relaxed = true)
    val storyRepository = mock<StoryRepository>(relaxed = true)
    val epicRepository = mock<EpicRepository>(relaxed = true)
    val iterationRepository = mock<IterationRepository>(relaxed = true)
    val attachmentRepository = mock<AttachmentRepository>(relaxed = true)
    val changeHistoryRepository = mock<ChangeHistoryRepository>(relaxed = true)
    val storyEvents = mock<StoryEvents>(relaxed = true)

    inline fun <reified T: Any> create() = registry.create(T::class)

    inline fun <reified T: Any> mock(relaxed: Boolean = false, crossinline block: T.() -> Unit = {}) =
      mockk(relaxed = relaxed, relaxUnitFun = true, block = block).also { registry.register(T::class, it) }
  }

  init {
    clearAllMocks()
    exchange.apply {
      every { redirect(any<String>(), any()) } throws StatusCodeException(Found)
      every { redirect(any<URI>(), any()) } throws StatusCodeException(Found)
      every { fullUrl(any()) } answers { URI("https://host" + firstArg<String>()) }
    }

    userRepository.apply {
      every { get(admin.id) } returns admin
      every { get(user.id) } returns user
    }

    projectRepository.apply {
      every { get(project.id) } returns project
    }

    storyRepository.apply {
      every { list(project.id) } returns listOf(story, story2)
      every { storyRepository.get(story.id) } returns story
    }

    epicRepository.apply {
      every { list(Epic::projectId to project.id) } returns listOf(epic)
      every { epicRepository.get(epic.id) } returns epic
    }

    iterationRepository.apply {
      every { list(project.id) } returns listOf(iteration)
    }

    changeHistoryRepository.apply {
      every { list(project.id) } returns listOf(change)
    }

    attachmentRepository.apply {
      every { file(project.id, story.id, "file") } returns java.nio.file.Path.of("build.gradle.kts")
    }

  }
}
