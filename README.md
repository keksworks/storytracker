# StoryTracker

Pivotal Tracker replacement for project management in Codeborne.

Built with:
* [Database](db): PostgreSQL
* [Backend](src): Kotlin/Klite
* [Unit tests](test): JUnit
* [Frontend](ui): Svelte/TailwindCSS/TypeScript, including:
  * Internationalization
  * TypeScript types generation
  * Unit tests
* [Development config](.env)

Prerequisites (install these first):
* JDK 21+
* Node 20+
* Docker

## Building

See [Dockerfile](Dockerfile) for more information

### Build server

`./gradlew jar`

### Build UI

Inside of `ui` directory:

Install the dependencies using: `npm install` and

Build UI using: `npm run build`

## Development

Start API by running the [Launcher](src/Launcher.kt).

It will automatically try to start the database using `docker compose up -d db`
To access DB via IDE use credentials from [.env](.env) file.

Start UI using: `cd ui && npm run dev`

## Running tests

Server tests:
`./gradlew test`

Repository tests are integration tests, connecting to the real database, which runs in Docker.
The test database name is `cos_test`.

UI tests:
`cd ui && npm test`

## Deployment instructions

The easiest way to deploy is via Docker compose:

`docker compose up -d`

This will build and start the application and the database.
