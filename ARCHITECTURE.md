# Architecture & Design Decisions

## Overview

Golf Performance Tracker uses **Clean Architecture** split across four Gradle modules. Room is the **Single Source of Truth**; the UI is driven by **StateFlow** and **Paging 3**.

```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│   :app   │────▶│   :ui    │────▶│ :domain  │◀────│  :data   │
│  shell   │     │ MVVM/UI  │     │ contracts│     │ Room/API │
└──────────┘     └──────────┘     └──────────┘     └──────────┘
```

## Module Responsibilities

| Module | Responsibility |
|--------|----------------|
| `:domain` | `Player`, `Shot`, `PlayerStatsSummary`; `GolfRepository` interface |
| `:data` | Retrofit, Room DAOs, `GolfRepositoryImpl`, `MockGolfInterceptor`, paging sources |
| `:ui` | Fragments, ViewModels, ViewBinding layouts, Navigation, custom chart views |
| `:app` | `Application`, Koin bootstrap, `NetworkConfig`, launcher icon |

**Dependency rule:** `:ui` and `:data` depend on `:domain` only. `:ui` never imports Retrofit or Room directly.

## REST API Strategy

The app is built around **Retrofit + Moshi** with a real HTTP contract. Demo data works out of the box without hosting a backend.

### Endpoints

| Method | Path | Purpose |
|--------|------|---------|
| `GET` | `/players` | All players with aggregate metrics |
| `GET` | `/players/{id}/shots` | Shots for one player |

Defined in `GolfApiService`; base URL from `BuildConfig.API_BASE_URL` in `app/build.gradle.kts`.

### Request flow

```
ViewModel.refresh()
    → GolfRepositoryImpl.refreshPlayers()
        → GolfApiService.getPlayers()          // Retrofit suspend call
            → OkHttp (MockGolfInterceptor)
                → 1. Attempt real HTTP to API_BASE_URL
                → 2. On success: return remote JSON
                → 3. On failure/404: return embedded demo JSON
        → playerDao.upsertAll()                // persist to Room
    → UI observes Room Flow / PagingData
```

### Why `MockGolfInterceptor`?

The default `API_BASE_URL` is a **placeholder** MockAPI project ID. In practice:

- **First launch:** Retrofit tries the network → fails → interceptor returns 5 players × 12 shots each.
- **With a real MockAPI:** Create resources matching the DTO shape; successful responses bypass the fallback.
- **Offline after sync:** Room serves cached data; no network needed.

This satisfies the assessment requirement for a mock *or* real REST API while keeping the project runnable for reviewers without setup.

### Switching to a live API

1. Create a [MockAPI](https://mockapi.io/) project with `players` and `players/{id}/shots` resources.
2. Update `API_BASE_URL` in `app/build.gradle.kts`:
   ```kotlin
   buildConfigField("String", "API_BASE_URL", "\"https://YOUR_ID.mockapi.io/\"")
   ```
3. Match JSON fields to `PlayerDto` / `ShotDto` in the `:data` module.
4. (Optional) Remove `MockGolfInterceptor` from `DataModule` for production builds.

## Key Decisions

### 1. Single Source of Truth (Room)

All displayed data flows from Room. Network writes to DB; UI observes DB (Flow or Paging). The UI never reads Retrofit responses directly.

### 2. Paging 3 for Shots

Player detail loads shots via `Pager` + Room `PagingSource` (page size 5). With 12 mock shots per player, scrolling triggers a second page load — pagination is visible during demo.

### 3. Statistics Screen

`PlayerStatsFragment` observes `PlayerStatsSummary` computed in the repository from cached shots:

- Summary cards (avg/max/min speed, shot count)
- `SimpleBarChartView` — custom Canvas bar chart for speed trend
- Material `LinearProgressIndicator` for average carry distance

### 4. Offline-First Sync

| Trigger | Action |
|---------|--------|
| Empty DB on launch | Force sync players |
| Network restored | Background sync players |
| Pull-to-refresh | Force sync players |
| Player detail opened | Sync that player's shots |

Failed sync with cached data shows an offline banner; users keep browsing the last sync.

### 5. Dependency Injection (Koin)

- `dataModule` — network, database, repository
- `uiModule` — ViewModels
- `app` — `NetworkConfig` from `BuildConfig`

ViewModel support is included via `koin-android` (no separate `koin-androidx-viewmodel` artifact in Koin 3.5+).

### 6. UI Binding Choice

The spec mentions DataBinding; this project uses **ViewBinding + StateFlow**, which is the modern equivalent: type-safe views, unidirectional state from ViewModels, collected in `repeatOnLifecycle`.

## Data Flow

```
Remote API ──▶ Repository ──▶ Room ──▶ Flow / PagingData ──▶ ViewModel ──▶ Fragment
                  ▲
         NetworkMonitor (sync on reconnect)
```

## Testing

| Module | Tests |
|--------|-------|
| `:data` | `GolfMappersTest` — DTO → entity → domain mapping |
| `:ui` | `PlayerListViewModelTest` — search/filter with Turbine + MockK |

Run: `./gradlew testDebugUnitTest`

## Assignment Requirements Mapping

| Requirement | Implementation |
|-------------|----------------|
| REST API | Retrofit + Moshi + OkHttp |
| Room offline cache | `GolfDatabase`, SSOT pattern |
| MVVM + Repository | ViewModels + `GolfRepository` |
| Search / filter | `PlayerListViewModel` |
| Coroutines + Flow | Repository + ViewModels |
| Material + light/dark | `values/` + `values-night/` themes |
| MotionLayout animation | Expandable stats card on detail |
| Custom metric views | `PerformanceMetricBarView`, `SimpleBarChartView` |
| Paging 3 (bonus) | `ShotPagingAdapter` + Room paging source |
| Stats screen (bonus) | `PlayerStatsFragment` |
| Multi-module (bonus) | `:app`, `:domain`, `:data`, `:ui` |

## Trade-offs

| Decision | Rationale |
|----------|-----------|
| Four modules vs one | Scalable structure; `:domain` is JVM-pure and testable |
| Koin over Hilt | No Gradle plugin; faster assessment setup |
| Mock interceptor | Reviewers can run without provisioning MockAPI |
| ViewBinding over DataBinding | Simpler, type-safe; pairs cleanly with StateFlow |
| Page size 5 | Makes pagination visible with 12 mock shots |

## Demo Screenshots & Video

**Screen recording:** [docs/demo/app-demo.webm](docs/demo/app-demo.webm) (also embedded in [README.md](README.md#app-demo))

Capture stills before submission and save to [`docs/screenshots/`](docs/screenshots/). See [`docs/screenshots/README.md`](docs/screenshots/README.md) for capture steps.

| # | Screen | File (suggested) |
|---|--------|----------------|
| 1 | Player list with search/filter | `docs/screenshots/01_player_list.png` |
| 2 | Player detail + shots | `docs/screenshots/02_player_detail.png` |
| 3 | Stats chart screen | `docs/screenshots/03_stats_chart.png` |
| 4 | Offline mode (airplane) | `docs/screenshots/04_offline_mode.png` |

<!-- Uncomment after adding images:
![Player list](docs/screenshots/01_player_list.png)
![Player detail](docs/screenshots/02_player_detail.png)
![Stats chart](docs/screenshots/03_stats_chart.png)
![Offline mode](docs/screenshots/04_offline_mode.png)
-->

### Demo video (optional)

_Add link here after recording a 30–60s walkthrough._
