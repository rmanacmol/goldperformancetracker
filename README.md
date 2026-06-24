# Golf Performance Tracker

An Android app for browsing golf players and their shot performance metrics. Built with **Clean Architecture**, **MVVM**, and an **offline-first** data layer.

This README explains **what languages and libraries we use**, **how the project is organized**, and **where to start reading the code** if you are reviewing or extending it.

For deeper design rationale, see [ARCHITECTURE.md](ARCHITECTURE.md).

---

## App demo

Screen recording of the full app walkthrough (player list → search/filter → detail → stats → offline banner):

<video src="docs/demo/app-demo.webm" controls width="100%">
  Your browser does not support embedded video. <a href="docs/demo/app-demo.webm">Download the demo video</a>.
</video>

Direct link: [docs/demo/app-demo.webm](docs/demo/app-demo.webm)

**What the demo shows:**
- Player list with search and club filter
- Player detail with paginated shots and expandable stats card
- Stats chart screen (toolbar chart icon)
- Pull-to-refresh sync

---

## Table of contents

1. [App demo](#app-demo)
2. [What this app does](#what-this-app-does)
3. [Languages & technologies](#languages--technologies)
4. [Project structure (how to read it)](#project-structure-how-to-read-it)
5. [Module-by-module guide](#module-by-module-guide)
6. [Screen map & navigation](#screen-map--navigation)
7. [Data flow (end to end)](#data-flow-end-to-end)
8. [Where to start reading](#where-to-start-reading)
9. [Setup & build](#setup--build)
10. [Testing](#testing)
11. [Further reading](#further-reading)

---

## What this app does

| Screen | Purpose |
|--------|---------|
| **Player list** | Shows golfers with club, avg speed, and carry distance. Supports search and club filter. |
| **Player detail** | Shows player stats, expandable stats card (MotionLayout), and paginated shot history. |
| **Player stats** | Bar chart of ball speed trend + carry distance progress indicator. |

Data is fetched over **REST**, cached in **Room**, and shown from cache when offline.

---

## Languages & technologies

### Core language

| Technology | Role in this project |
|------------|----------------------|
| **Kotlin** | All application logic. Primary language for ViewModels, repositories, models, and tests. |
| **XML** | Android layouts (`res/layout/`), navigation graph, themes, strings, and MotionLayout scenes. |
| **Gradle Kotlin DSL** (`.kts`) | Build configuration (`build.gradle.kts`, `settings.gradle.kts`, `libs.versions.toml`). |

We do **not** use Java source files or Jetpack Compose in this project. UI is built with **Fragments + ViewBinding + XML layouts**.

### Android & Jetpack

| Library | What it does here |
|---------|-------------------|
| **AndroidX / Jetpack** | Core Android APIs (`core-ktx`, `appcompat`). |
| **Lifecycle + ViewModel** | Survives configuration changes; holds UI state. |
| **Navigation Component** | Fragment navigation (`nav_graph.xml`) with typed arguments (`playerId`). |
| **Room** | SQLite database for offline cache (players, shots, sync metadata). |
| **Paging 3** | Loads player shots in pages of 5 from Room. |
| **Material Components** | Toolbar, cards, chips, text fields, progress indicators, themes. |
| **SwipeRefreshLayout** | Pull-to-refresh on the player list. |
| **MotionLayout** | Expand/collapse animation on the player detail stats card. |

### Async & reactive

| Library | What it does here |
|---------|-------------------|
| **Kotlin Coroutines** | `suspend` network/DB calls, background sync, ViewModel scopes. |
| **Kotlin Flow** | Reactive streams from Room and repository; collected in UI with `repeatOnLifecycle`. |
| **StateFlow** | ViewModels expose immutable `StateFlow<UiState>` for one-way UI binding. |

### Networking & JSON

| Library | What it does here |
|---------|-------------------|
| **Retrofit 2** | Declarative REST API (`GolfApiService`). |
| **Moshi** | JSON → Kotlin data classes (`PlayerDto`, `ShotDto`). |
| **OkHttp** | HTTP client; includes logging interceptor and `MockGolfInterceptor` fallback. |

### Dependency injection

| Library | What it does here |
|---------|-------------------|
| **Koin** | Wires dependencies without a Gradle plugin. Modules: `dataModule`, `uiModule`, plus `NetworkConfig` in `GolfApp`. |

### Images & logging

| Library | What it does here |
|---------|-------------------|
| **Glide** | Loads player avatar URLs with placeholder and cross-fade. |
| **Timber** | Debug logging for sync events and errors. |

### Testing

| Library | What it does here |
|---------|-------------------|
| **JUnit 4** | Unit test runner. |
| **MockK** | Mocks `GolfRepository` in ViewModel tests. |
| **Turbine** | Asserts on `Flow` / `StateFlow` emissions in tests. |

### Version catalog

Dependencies are centralized in:

```
gradle/libs.versions.toml
```

Each module references them as `libs.retrofit`, `libs.room.runtime`, etc.

---

## Project structure (how to read it)

The repo is a **multi-module Gradle project**. Dependencies flow **inward**: UI and data depend on domain; domain depends on nothing Android-specific.

```
goldperformancetracker/
├── app/                    # Application entry point
├── domain/                 # Business models & contracts (pure Kotlin)
├── data/                   # API, Room, repository implementation
├── ui/                     # Screens, ViewModels, layouts, navigation
├── gradle/
│   └── libs.versions.toml  # Shared dependency versions
├── docs/
│   ├── demo/
│   │   └── app-demo.webm     # Screen recording walkthrough
│   └── screenshots/          # Place demo screenshots here
├── README.md               # This file
└── ARCHITECTURE.md         # Design decisions & API strategy
```

### Dependency graph

```
┌──────────┐
│   :app   │  GolfApp, launcher icon, BuildConfig (API URL)
└────┬─────┘
     │ depends on
     ├── :ui ──────▶ :domain
     └── :data ────▶ :domain
```

**Rule:** `:domain` never imports Retrofit, Room, or Android UI classes. That keeps business logic testable and portable.

---

## Module-by-module guide

### `:app` — Application shell

```
app/src/main/java/com/renz/golfperformancetracker/
└── GolfApp.kt              # Application class; starts Koin, plants Timber
```

**Read this for:** app startup, Koin module registration, `API_BASE_URL` from `BuildConfig`.

`app/build.gradle.kts` defines `applicationId`, `API_BASE_URL`, and launcher resources.

---

### `:domain` — Business layer (pure Kotlin)

```
domain/src/main/kotlin/com/renz/golfperformancetracker/domain/
├── model/
│   └── Models.kt           # Player, Shot, PlayerStatsSummary, SyncStatus
└── repository/
    └── GolfRepository.kt   # Interface the UI depends on
```

**Read this for:** what data the app works with and what operations are available — without implementation detail.

Key types:

- `Player` — id, name, club, avatar, aggregate metrics
- `Shot` — per-swing metrics (speed, launch angle, carry, spin, club, timestamp)
- `GolfRepository` — `observePlayers()`, `pagingShots()`, `refreshPlayers()`, etc.

---

### `:data` — Data layer

```
data/src/main/java/com/renz/golfperformancetracker/
├── data/
│   ├── di/DataModule.kt           # Koin: Retrofit, Room, repository
│   ├── NetworkConfig.kt           # API base URL + debug flag
│   ├── local/
│   │   ├── GolfDatabase.kt        # Room database definition
│   │   ├── dao/GolfDaos.kt        # Player, Shot, sync DAOs + PagingSource
│   │   └── entity/                # Room table entities
│   ├── remote/
│   │   ├── GolfApiService.kt      # Retrofit endpoints
│   │   ├── MockGolfInterceptor.kt # Fallback JSON when API unavailable
│   │   └── dto/GolfDtos.kt        # Network JSON models
│   ├── mapper/GolfMappers.kt      # DTO ↔ Entity ↔ Domain mappers
│   └── repository/
│       └── GolfRepositoryImpl.kt  # Single source of truth implementation
└── util/
    └── NetworkMonitor.kt          # Connectivity Flow for offline sync
```

**Read this for:** how data is fetched, stored, synced, and exposed as Flow/PagingData.

Important pattern: **Room is the single source of truth.** Network responses are written to Room; the UI never reads Retrofit directly.

---

### `:ui` — Presentation layer

```
ui/src/main/java/com/renz/golfperformancetracker/ui/
├── MainActivity.kt                # Single activity; hosts NavHostFragment
├── di/UiModule.kt                 # Koin ViewModel bindings
├── players/
│   ├── list/
│   │   ├── PlayerListFragment.kt
│   │   ├── PlayerListViewModel.kt
│   │   ├── PlayerListUiState.kt
│   │   └── PlayerAdapter.kt
│   └── detail/
│       ├── PlayerDetailFragment.kt
│       ├── PlayerDetailViewModel.kt
│       ├── PlayerDetailUiState.kt
│       └── ShotPagingAdapter.kt
├── stats/
│   ├── PlayerStatsFragment.kt
│   ├── PlayerStatsViewModel.kt
│   └── PlayerStatsUiState.kt
└── common/
    ├── PerformanceMetricBarView.kt  # Custom metric bars on list/shot cards
    └── SimpleBarChartView.kt        # Custom bar chart on stats screen

ui/src/main/res/
├── layout/          # fragment_*.xml, item_*.xml, activity_main.xml
├── navigation/      # nav_graph.xml
├── values/          # strings, colors, themes, dimens
├── values-night/    # Dark theme overrides
├── menu/            # Toolbar menus
├── anim/            # Fragment transition animations
└── xml/             # MotionLayout scene_player_stats.xml
```

**Read this for:** screens, user interactions, and how ViewModels connect to the repository.

Each feature follows **MVVM**:

```
Fragment  →  collects StateFlow / PagingData
ViewModel →  calls GolfRepository, exposes UiState
UiState   →  data class holding everything the screen needs to render
```

---

## Screen map & navigation

Defined in `ui/src/main/res/navigation/nav_graph.xml`:

```
PlayerListFragment  ──(playerId)──▶  PlayerDetailFragment  ──(playerId)──▶  PlayerStatsFragment
     ▲                                        │
     └──────────── back ──────────────────────┘
```

| Fragment | ViewModel | Key layout |
|----------|-----------|------------|
| `PlayerListFragment` | `PlayerListViewModel` | `fragment_player_list.xml` |
| `PlayerDetailFragment` | `PlayerDetailViewModel` | `fragment_player_detail.xml` |
| `PlayerStatsFragment` | `PlayerStatsViewModel` | `fragment_player_stats.xml` |

Navigation passes `playerId` as a Safe Args–style string argument (via `Bundle` / `SavedStateHandle`).

---

## Data flow (end to end)

Example: user opens the app and sees the player list.

```
1. PlayerListViewModel.init → repository.refreshPlayers()
2. GolfRepositoryImpl → GolfApiService.getPlayers()  (Retrofit)
3. OkHttp tries network → MockGolfInterceptor returns JSON if API fails
4. Repository maps DTOs → Room entities → playerDao.upsertAll()
5. UI collects repository.observePlayers()  (Flow from Room)
6. ViewModel combines players + search filter → PlayerListUiState
7. Fragment collects uiState → PlayerAdapter.submitList()
```

Offline behavior:

```
NetworkMonitor.isOnline = false  →  offline banner shown
Room still has data              →  list/detail/stats keep working
Network restored                 →  repository auto-syncs players
```

---

## Where to start reading

Recommended order for a code review:

| Step | File | Why |
|------|------|-----|
| 1 | `domain/model/Models.kt` | Understand domain objects |
| 2 | `domain/repository/GolfRepository.kt` | See the app’s data contract |
| 3 | `data/repository/GolfRepositoryImpl.kt` | See sync + SSOT logic |
| 4 | `data/remote/GolfApiService.kt` + `MockGolfInterceptor.kt` | See API shape and fallback |
| 5 | `data/local/GolfDatabase.kt` + `GolfDaos.kt` | See persistence |
| 6 | `ui/players/list/PlayerListViewModel.kt` | See MVVM + Flow combine |
| 7 | `ui/players/list/PlayerListFragment.kt` | See UI collection pattern |
| 8 | `app/.../GolfApp.kt` + `data/di/DataModule.kt` | See DI wiring |
| 9 | `ui/src/main/res/navigation/nav_graph.xml` | See screen flow |

For a **single vertical slice**, trace **“tap player → see shots”**:

`PlayerListFragment` → `PlayerDetailFragment` → `PlayerDetailViewModel` → `GolfRepository.pagingShots()` → `ShotDao.pagingSource()` → `ShotPagingAdapter`

---

## Setup & build

### Requirements

- Android Studio Ladybug (2024.2+) or newer
- **JDK 17+**
- Android SDK 35
- Emulator or device (API 24+)

### Run

```bash
# Clone and open in Android Studio, then:
./gradlew installDebug
```

### Optional: use a real MockAPI backend

Edit `API_BASE_URL` in `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://YOUR_PROJECT.mockapi.io/\"")
```

Expected endpoints: `GET /players`, `GET /players/{id}/shots`.  
If the URL is unreachable, `MockGolfInterceptor` serves embedded demo data automatically.

---

## Testing

```bash
./gradlew testDebugUnitTest
```

| Test file | Module | What it verifies |
|-----------|--------|------------------|
| `GolfMappersTest.kt` | `:data` | DTO → Entity → Domain mapping |
| `PlayerListViewModelTest.kt` | `:ui` | Search/filter logic with Turbine + MockK |

---

## Further reading

| Document | Contents |
|----------|----------|
| [docs/demo/app-demo.webm](docs/demo/app-demo.webm) | Screen recording — full app walkthrough |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Design decisions, REST API strategy, requirements mapping |
| [docs/screenshots/README.md](docs/screenshots/README.md) | How to capture demo screenshots |

---

## Quick reference — architecture pattern

```
┌─────────────────────────────────────────────────────────┐
│  UI (Fragment + ViewModel + UiState + ViewBinding)      │
└─────────────────────────┬───────────────────────────────┘
                          │ GolfRepository interface
┌─────────────────────────▼───────────────────────────────┐
│  Domain (models, repository contract)                   │
└─────────────────────────┬───────────────────────────────┘
                          │ GolfRepositoryImpl
┌─────────────────────────▼───────────────────────────────┐
│  Data (Retrofit, Room, mappers, NetworkMonitor)         │
└─────────────────────────────────────────────────────────┘
```

**Pattern:** MVVM + Repository + Single Source of Truth (Room) + offline-first sync.
