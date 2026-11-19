# Label Manager

A record label management game: start a label with 16 signed artists and a budget, manage the
roster, and buy and sell artist contracts on a live market where rival labels trade too.

The repository holds a native Android app and the REST API it talks to. The API is written once
in Kotlin and runs both as a real HTTP server and inside the app, so the app works out of the box
without any server, and switching it to the real server changes only the transport.

## Features

- **Accounts**: sign up and sign in with email and password; one label per account. Tokens are kept
  in an encrypted DataStore; the access token is refreshed transparently.
- **Your label**: 16 artists, four in each genre (pop, rock, hip-hop, electronic), aged 18–45, and
  a $3M budget. An artist's value starts at $400K plus $40K per chart hit, with a bonus for artists
  up to 25. Rename the label, change its country, rename artists and change their nationality.
- **Roster tab**: the label with its budget and value, artists grouped by genre, search that
  ignores case and accents. Cached in Room, so it opens instantly and offline.
- **Market**: list an artist with an asking price, buy other labels' artists for theirs. Filter
  by price, genre, artist country and label country, search by artist or label name, sort by date
  or price. After every deal both budgets change and the artist's value grows by 5% per chart hit,
  from 5% up to 50%.
- **Activity**: deals and listings of your label, grouped by day, with an unread badge, and a system
  notification when a rival label buys one of your artists.
- **Profile**: light, dark or system theme, sale alerts, app language, sign out.
- English, Russian, German, Spanish and French; light and dark themes; TalkBack labels.

## Project structure

| Module | What it is |
|---|---|
| `:contract` | The API contract in pure Kotlin: DTOs, routes, error codes, JSON configuration and limits, shared by the app and the backend. |
| `:backend` | The game itself in pure Kotlin: rules, auth, market, the rival-label simulation and `LabelBackend.handle(request)`. |
| `:server` | A Ktor (Netty) server around `:backend` with SQLite storage and Swagger UI at `/docs`. |
| `:app` | The Android app. |

More on the API: [docs/backend.md](docs/backend.md) and the OpenAPI spec in
[docs/api/openapi.yaml](docs/api/openapi.yaml).

### The app

- **Stack**: Kotlin, Jetpack Compose, Navigation 3, Hilt, Ktor client with kotlinx.serialization,
  Room, Paging 3, DataStore with Tink encryption, WorkManager; min SDK 24.
- **Packages**: `core/` for shared infrastructure (network, session, settings, design system),
  `features/<name>/` for features; each split into `data`, `domain` and `presentation` layers,
  with Hilt modules in `di/`.
- **Layers**: repositories talk to one source each (API, Room, preferences); interactors in the
  domain layer combine them, e.g. a purchase refreshes the cached roster, and signing in or out
  clears it. View models expose one `StateFlow` of view state, take user actions through a single
  `handleUiAction`, and send one-off events (navigation, results) through a channel.
- **Design system**: tokens (colors, type, spacing, shapes) and components in
  `core/design_system`, with previews for every state.

## Build flavors

| Flavor | API |
|---|---|
| `mock` (default) | The backend runs inside the app behind a Ktor `MockEngine`, with simulated latency and its data saved to a file. No server and no internet permission needed. |
| `remote` | A real server over HTTP, `http://10.0.2.2:8080/` (the host machine from the emulator) by default. |

## Running

Open the project in Android Studio and run the `app` configuration with the `mockDebug` variant,
or from the command line:

```
./gradlew :app:installMockDebug
```

To use the real server, start it and install the `remote` flavor:

```
./gradlew :server:run
./gradlew :app:installRemoteDebug
```

Another server address: `./gradlew :app:installRemoteDebug -PlabelManager.remoteBaseUrl=https://example.com/`.
The server reads `PORT`, `LABEL_DB_PATH`, `LABEL_JWT_SECRET` and `LABEL_SIMULATION` from the
environment.

## Release build

Release builds are minified, obfuscated and shrunk with R8:

```
./gradlew :app:assembleMockRelease
```

They are signed with the debug key so they can be installed for review.

## Tests

```
./gradlew :backend:test :app:testMockDebugUnitTest
```

- Unit tests for view models, interactors, repositories (with Ktor `MockEngine` and an in-memory
  Room database), paging sources and mappers.
- Screenshot tests with Roborazzi: every `@Preview` is rendered and compared with the reference
  images in `app/screenshots`. Record new references with
  `./gradlew :app:recordRoborazziMockDebug`, check with `./gradlew :app:verifyRoborazziMockDebug`.

GitHub Actions runs two jobs on every pull request and push to `main`: unit tests, and screenshot
tests that keep the images of any differences as an artifact.

## What could come next

- Add a release build and instrumented UI tests to CI.
- Push notifications from the server instead of periodic checks.
- Offline support for the market and the activity feed.
