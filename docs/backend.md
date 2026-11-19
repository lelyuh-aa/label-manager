# Backend

The Label Manager API is written once and runs in two places: as a real HTTP server and inside
the Android app's `mock` flavor. Both run the same Kotlin code, so anyone can install the app
and play without any server, and switching the app to the real server changes nothing but the
transport.

```
                    ┌──────────────────────────────┐
                    │ :contract                    │  DTOs, routes, error codes,
                    │ (pure Kotlin)                │  JSON configuration, limits
                    └──────────────┬───────────────┘
                                   │
                    ┌──────────────┴───────────────┐
                    │ :backend                     │  game rules, auth, market,
                    │ (pure Kotlin)                │  rival labels, LabelBackend
                    │ handle(ApiRequest): Response │
                    └───────┬──────────────┬───────┘
                            │              │
     ┌──────────────────────┴───┐      ┌───┴──────────────────────────┐
     │ app, mock flavor         │      │ :server                      │
     │ Ktor client MockEngine   │      │ Ktor server (Netty)          │
     │ InMemoryBackendStore     │      │ ExposedBackendStore          │
     │ + JSON snapshot file     │      │ + SQLite                     │
     └──────────────────────────┘      └──────────────┬───────────────┘
                                                      │ HTTP
                                       ┌──────────────┴───────────────┐
                                       │ app, remote flavor; any web  │
                                       │ or mobile front end; curl    │
                                       └──────────────────────────────┘
```

## Modules

| Module | What it holds |
| --- | --- |
| `:contract` | Request and response DTOs, `ApiRoutes`, `ApiQuery`, `ErrorCodeDto`, `ApiJson`, `ContractLimits`, `Countries`. Shared by every party. |
| `:backend` | The API itself, independent of any HTTP library. `LabelBackend.handle(ApiRequest): ApiResponse` is its only entry point. Runs on the JVM and on Android API 24+. |
| `:server` | A thin Ktor application that forwards every `/api` request to `LabelBackend`, plus SQLite storage, CORS, logging and the API docs. |

Inside `:backend`:

| Package | Role |
| --- | --- |
| `api` | `LabelBackend`, a small router with path templates, error mapping, DTO mappers |
| `domain` | models, `GameRules`, services (`AuthService`, `LabelService`, `ArtistService`, `MarketService`, `ActivityService`), `DealExecutor`, keyset paging, validation |
| `generation` | labels and artists with realistic names for 22 countries |
| `security` | PBKDF2 password hashing, HS256 access tokens, rotating refresh tokens |
| `simulation` | rival labels: seeding and the market simulation |
| `data` | `BackendStore` and its in-memory implementation |

## Rules

| Rule | Where |
| --- | --- |
| One account per email, case-insensitive; one label per account | `AuthService.register` |
| New label: 16 artists, 4 per genre (pop, rock, hip-hop, electronic) | `GameRules`, `LabelGenerator` |
| Budget $3,000,000; age random 18–45; value $400,000 + $40,000 per hit, +$150,000 up to age 25 | `GameRules` |
| Label value = sum of artist values | `LabelView.value` |
| Owner edits label name and country, artist first name, last name and country | `LabelService`, `ArtistService` |
| Owner lists an artist with an asking price; one open listing per artist; can withdraw | `MarketService`, `DealExecutor.list` |
| Market filters: artist name, label name, artist country, label country, price range, genres | `MarketService.listings` |
| Buyer pays exactly the asking price; both budgets change; value grows by 5 % per hit, 5–50 % | `DealExecutor.purchase`, `GameRules.valueAfterDeal` |
| Cannot buy own artist, cannot overspend, a sold listing is gone | `DealExecutor.purchase` |
| Two simultaneous buyers: exactly one wins, the other gets `409 LISTING_NOT_AVAILABLE` | one store transaction per purchase |

Every failure has a machine-readable code (`ErrorCodeDto`) and a fixed HTTP status, documented in
[`docs/api/openapi.yaml`](api/openapi.yaml).

## Storage

`BackendStore` offers two operations, `read { }` and `write { }`. A write block is atomic: if it
throws, nothing it changed is kept. The interface is deliberately small (find and save per entity),
so a new database costs one class.

- **In the app** — `InMemoryBackendStore`: state in memory, writes serialised by a mutex and applied
  to a copy that replaces the state only after the block succeeds. With a snapshot file the whole
  state is written as JSON after each write (temporary file, then rename) and read back lazily on
  the first request.
- **On the server** — `ExposedBackendStore`: SQLite through Exposed, one serializable transaction per
  call, WAL journal, writes queued in process because SQLite has a single writer. The file is
  `server/data/label-manager.db` by default.

## Rival labels

A market with one user would be empty, so the backend runs rival labels.

- **First start** — 24 rival labels are created with full rosters, 40 past deals between them
  (artists already have a history and grown values) and 30 open listings of various ages.
- **Every 2 simulated minutes** — a rival may list an artist at 95–160 % of its value; rivals may buy
  each other's listings; every user listing older than 4 minutes may catch a rival's interest. The
  chance falls as the asking price rises above the market value and is zero above twice the value.
  Rivals never pay more than their budget.
- **Same rules** — every deal goes through `DealExecutor`, exactly like a user's purchase, so the
  user gets the money, the activity event and the value increase as in any other sale.
- **Lazy replay** — time is replayed on every request (and on a 30-second timer on the server), at
  most 90 ticks at once; a longer pause skips the rest. With a seeded `Random` and a fixed clock the
  outcome is reproducible, which the tests rely on.

## Running the server

```bash
./gradlew :server:run
```

| Variable | Default | Meaning |
| --- | --- | --- |
| `PORT` | `8080` | HTTP port |
| `LABEL_DB_PATH` | `data/label-manager.db` (relative to `server/`) | SQLite file |
| `LABEL_JWT_SECRET` | development key, with a warning in the log | access token signing key |
| `LABEL_SIMULATION` | `on` | `off` disables the rival labels |

- Swagger UI: <http://localhost:8080/docs>; the raw OpenAPI file: <http://localhost:8080/openapi.yaml>.
- The Android emulator reaches the host machine at `http://10.0.2.2:8080`, the default `BASE_URL` of
  the app's `remote` flavor. For a phone on the same network, pass the machine's address:
  `./gradlew :app:installRemoteDebug -PlabelManager.remoteBaseUrl=http://192.168.1.20:8080/`.
- A self-contained jar for deployment: `./gradlew :server:buildFatJar`, then
  `java -jar server/build/libs/label-manager-server.jar`.

## Using the in-process backend

The app's `mock` flavor builds the backend once and answers Ktor client requests from it:

```kotlin
val backend = LabelBackendFactory.inMemory(snapshotFile = File(context.filesDir, "backend.json"))

val engine = MockEngine { request ->
    val response = backend.handle(
        ApiRequest(
            method = request.method.value,
            path = request.url.encodedPath,
            query = request.url.parameters.entries().associate { it.key to it.value },
            headers = request.headers.entries().associate { it.key to it.value.joinToString(",") },
            body = (request.body as? TextContent)?.text,
        )
    )
    respond(response.body.orEmpty(), HttpStatusCode.fromValue(response.status), headersOf(...))
}
```

The first request seeds the rival labels and writes the snapshot, so it takes noticeably longer than
the rest; later requests are in-memory operations plus one file write per change.

## Walkthrough with curl

```bash
# 1. sign up: a label of 16 artists and a budget are created
curl -s -X POST localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"coach@example.com","password":"password1"}'
# → {"accessToken":"eyJ…","accessTokenExpiresAt":"…","refreshToken":"…","user":{…}}

TOKEN=eyJ…   # the accessToken from the response

# 2. the label with its roster, budget and value
curl -s localhost:8080/api/v1/label -H "Authorization: Bearer $TOKEN"

# 3. put an artist on the market
curl -s -X POST localhost:8080/api/v1/market/listings \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"artistId":"<artist id>","askingPrice":1500000}'

# 4. browse the market: hip-hop up to $2M from German labels, cheapest first
curl -s "localhost:8080/api/v1/market/listings?genres=HIP_HOP&maxPrice=2000000&labelCountry=DE&sort=PRICE_ASCENDING" \
  -H "Authorization: Bearer $TOKEN"

# 5. buy a listing
curl -s -X POST localhost:8080/api/v1/market/listings/<listing id>/purchase \
  -H "Authorization: Bearer $TOKEN"

# 6. what happened to the label, including sales to rival labels
curl -s localhost:8080/api/v1/activity -H "Authorization: Bearer $TOKEN"
```

## Tests

```bash
./gradlew :contract:test :backend:test :server:test
```

- `:backend` — unit tests for every rule above: generation, hashing and tokens, the store, each
  service, the purchase race, paging, the simulation, and end-to-end tests of every endpoint
  through `LabelBackend.handle`.
- `:server` — the SQLite store, and HTTP tests through Ktor's `testApplication`: a trade between two
  users, the purchase race, error bodies, CORS, the docs and a restart.
