import auth.*
import db.Id
import db.initDB
import klite.*
import klite.annotations.annotated
import klite.http.httpClient
import klite.jdbc.RequestTransactionHandler
import klite.jobs.JobRunner
import klite.json.JsonBody
import klite.oauth.AuthRoutes
import klite.oauth.GoogleOAuthClient
import klite.oauth.OAuthRoutes
import klite.oauth.OAuthUserProvider
import kotlinx.coroutines.launch
import stories.Epic
import stories.EpicRepository
import stories.PivotalImporter
import stories.ProjectMember
import stories.ProjectMemberRepository
import stories.ProjectRepository
import stories.ProjectRoutes
import users.UserRepository
import users.UserRoutes
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.reflect.full.primaryConstructor
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

val Config.isE2E get() = isActive("e2e")

val assetsPath = Path.of("ui/build").takeIf { it.exists() } ?: Path.of("ui/public")

fun startServer() = Server(
  sessionStore = CookieSessionStore(cookie = Cookie("S", "", httpOnly = true, secure = Config.isProd, maxAge = 365.days)),
  httpExchangeCreator = XForwardedHttpExchange::class.primaryConstructor!!
).apply {
  initDB()

  register(httpClient())

  use<JsonBody>()
  use<RequestTransactionHandler>()

  assets("/", AssetsHandler(assetsPath, useIndexForUnknownPaths = true))

  context("/oauth") {
    register<OAuthUserProvider>(AuthUserProvider::class)
    register<GoogleOAuthClient>()
    annotated<OAuthRoutes>()
  }

  context("/api") {
    post("/js-error") { logger("js-error").error(rawBody) }

    if (Config.isDev || Config.isE2E) annotated<FakeAuthRoutes>()

    before<AccessChecker>()
    useHashCodeAsETag()
    useOnly<JsonBody>()

    annotated<AuthRoutes>(annotations = listOf(Public()))
    annotated<UserRoutes>("/users")
    annotated<ProjectRoutes>("/projects")
  }

  start()
}

fun Server.startJobs() {
  use(require<JobRunner>().apply {
    val count = require<ProjectRepository>().count()
    schedule(require<PivotalImporter>(), period = 3.hours, delay = 0.hours)// if (count == 0L) 0.hours else 1.hours)
  })
}

fun main() {
  Config.useEnvFile()
  startServer().startJobs()
}
