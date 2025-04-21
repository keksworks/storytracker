import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "2.1.20"
}

repositories {
  mavenLocal()
  mavenCentral()
  maven { url = uri("https://jitpack.io") }
}

dependencies {
  fun klite(module: String) = "com.github.codeborne.klite:klite-$module:1.6.16"
  implementation(klite("server"))
  implementation(klite("json"))
  implementation(klite("i18n"))
  implementation(klite("jdbc"))
  implementation(klite("slf4j"))
  implementation(klite("csv"))
  implementation("org.postgresql:postgresql:42.7.5")

  testImplementation(klite("jdbc-test"))
  testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
  testRuntimeOnly("org.junit.jupdbiter:junit-jupiter-engine:5.11.4")
  testImplementation("ch.tutteli.atrium:atrium-fluent:1.3.0-alpha-1")
  testImplementation("io.mockk:mockk:1.13.16")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
}

sourceSets {
  main {
    kotlin.setSrcDirs(listOf("src"))
    resources.setSrcDirs(listOf("src", "db", "ui/i18n")).exclude("**/*.kt")
  }
  test {
    kotlin.setSrcDirs(listOf("test"))
    resources.setSrcDirs(listOf("test")).exclude("**/*.kt")
  }
}

tasks.test {
  workingDir(rootDir)
  useJUnitPlatform()
  // enable JUnitAssertionImprover from klite.jdbc-test
  jvmArgs("-DENV=test", "-Djunit.jupiter.extensions.autodetection.enabled=true", "--add-opens=java.base/java.lang=ALL-UNNAMED", "-XX:-OmitStackTraceInFastThrow")
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "21"
  if (System.getProperty("user.name") != "root") finalizedBy("types.ts")
}

tasks.register<Copy>("deps") {
  into("$buildDir/libs/deps")
  from(configurations.runtimeClasspath)
}

val mainClassName = "LauncherKt"

tasks.jar {
  dependsOn("deps")
  doFirst {
    manifest {
      attributes(
        "Main-Class" to mainClassName,
        "Class-Path" to File("$buildDir/libs/deps").listFiles()?.joinToString(" ") { "deps/${it.name}"}
      )
    }
  }
}

tasks.register<JavaExec>("run") {
  workingDir(rootDir)
  jvmArgs("--add-exports=java.base/sun.net.www=ALL-UNNAMED")
  mainClass.set(mainClassName)
  classpath = sourceSets.main.get().runtimeClasspath
}

tasks.register<JavaExec>("types.ts") {
  dependsOn("classes")
  mainClass.set("klite.json.TSGenerator")
  classpath = sourceSets.test.get().runtimeClasspath
  args("${project.buildDir}/classes/kotlin/main",
    "-o", project.file("ui/src/api/types.ts"),
    "-p", """
      export type Id<T extends Entity<T>> = string & {_of?: T}
      export type TSID<T extends Entity<T>> = Id<T>
      export type Entity<T extends Entity<T>> = {id: Id<T>}
    """.trimIndent() + "\n",
    "java.time.DayOfWeek",
    "-t", "db.TestData"
  )
}
