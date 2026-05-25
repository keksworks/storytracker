* Prefer simple code
* No semicolons
* Follow existing formatting
* Avoid duplication - extract similar code and reuse
* Use camelCase for fields, in DB/Entities/JSON to avoid conversions
* Backend code is written in Kotlin using Klite framework; code must be short and concise
  * Klite guide: https://raw.githubusercontent.com/keksworks/klite/refs/heads/main/llms.txt
  * Gradle source directories are simplified (just `src` & `test`)
  * In Kotlin, prefer expression body functions
  * Repositories can extend `db.CrudRepository` to avoid implementing all the basic operations
  * Repository tests extend `db.DBTest` and run on a real database
  * Other tests are pure unit tests
  * `expect(subject).toEqual(expected)`
  * In tests, prefer using pre-created entities from `test/db/TestData.kt`; modify only needed fields using .copy()
  * Every new entity should also be added to TestData for reuse in tests
  * In expectations, also prefer using .copy() and full equality checks instead of field-by-field
  * Always add tests for new backend functions
  * Route handlers are in app module and should be registered in src/app/ApiRoutes.kt
  * For route/service layers tests, extend `test/db/BaseMocks.kt` to avoid duplicating mock creation and basic setup
  * If there are new repositories/services, ass them to BaseMocks for reuse
  * PostgreSQL database changesets are in `db` directory following file per table structure
* Frontend is in `ui` directory and written using Svelte+TypeScript with TailwindCSS
  * Prefer Svelte 4 syntax (without runes), use event handlers without colons
  * Tests written using vitest; every new component need to have at least one basic render test
  * Backend converts empty strings into nulls when reading json, meaning that no normalization after binding from forms is necessary. FormField/Svelte inputs accept undefined values as empty strings also, so no need to create empty objects with empty strings.
  * Backend types are converted to TypeScript interfaces for type safety in frontend using `./gradlew types.ts` and are stored in `ui/src/api/types.ts` - this is run automatically after backend compilation
  * In TS/JS prefer simple conditions, e.g. `if (some)`, not `if (some !== undefined)`
  * Avoid `null` unless necessary, prefer `undefined` and ? syntax for optional fields/arguments
* Always add translations for all supported languages in `ui/i18n`
* Never commit code yourself, give developer an opportunity to review changes first
