package db

import db.TestData.user
import db.TestData.viewer
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import klite.Config
import klite.DependencyInjectingRegistry
import klite.HttpExchange
import klite.StatusCode.Companion.Found
import klite.StatusCodeException
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
      every { get(user.id) } returns user
      every { get(viewer.id) } returns viewer
    }
  }
}
