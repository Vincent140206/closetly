# Closetly

Closetly is a Kotlin and Jetpack Compose university group project for personal wardrobe and outfit management. The `develop` branch is intentionally an **application-level foundation**, not a partially implemented product.

Each of the six assigned modules is developed independently on its existing feature branch. Shared infrastructure belongs on `develop` and should remain free of generated feature models, ViewModels, repositories, dummy data, and pretend CRUD flows.

## Currently implemented

- Android application and Compose initialization
- Material 3 light/dark theme and shared UI primitives
- Centralized, type-safe Navigation Compose graph
- Development Home launcher with six module entry points
- One reusable module ownership placeholder with functional Back navigation
- Login, Registration, and Profile route placeholders
- Lightweight application container
- Optional, lazy Supabase client configuration
- Gradle Kotlin DSL, version catalog, unit-test, and instrumentation-test infrastructure
- Reserved packages for all six individual modules

The app starts at Home and runs without Supabase credentials.

## Not implemented yet

- Digital Wardrobe functionality
- Mix & Match Outfit functionality
- Outfit Planner functionality
- Style Inspiration Board functionality
- Fashion Wishlist functionality
- Wear History functionality
- Feature-specific models, repositories, ViewModels, UI state, screens, or CRUD
- Real authentication or profile persistence
- Supabase database schema deployment or feature repositories
- Image selection, uploads, downloads, or storage policies

The module cards deliberately open a **Not implemented yet** screen. They do not represent completed features.

## Technology

- Kotlin 2.2.21
- Android Gradle Plugin 9.2.1 and Gradle 9.4.1
- Jetpack Compose and Material 3
- Navigation Compose with `@Serializable` routes
- Kotlinx Serialization
- Supabase Kotlin SDK modules for future Auth, PostgREST, and Storage integration
- Ktor Android engine
- Coil retained for planned image rendering
- minSdk 26, targetSdk 36, compileSdk 36.1

Dependency versions are centralized in `gradle/libs.versions.toml`.

## Project structure

```text
app/src/main/java/vin/pam/closetly/
├── MainActivity.kt
├── ClosetlyApp.kt
├── ClosetlyApplication.kt
├── core/
│   ├── config/AppConfig.kt
│   ├── data/SupabaseProvider.kt
│   ├── navigation/
│   │   ├── AppModule.kt
│   │   ├── AppNavHost.kt
│   │   └── Routes.kt
│   └── ui/
│       ├── components/
│       └── shell/
├── di/AppContainer.kt
├── feature/
│   ├── auth/                 # Shared placeholder only
│   ├── profile/              # Shared placeholder only
│   ├── wardrobe/.gitkeep
│   ├── outfit/.gitkeep
│   ├── planner/.gitkeep
│   ├── inspiration/.gitkeep
│   ├── wishlist/.gitkeep
│   └── wearhistory/.gitkeep
└── ui/theme/
```

The existing theme package remains at `ui/theme` to avoid a cosmetic move with no architectural benefit.

## Feature ownership

| Package | Module | Owner | Existing branch |
|---|---|---|---|
| `feature/wardrobe` | Digital Wardrobe | Vincent | `feature/digital-wardrobe` |
| `feature/outfit` | Mix & Match Outfit | Janice | `feature/mix-and-match` |
| `feature/planner` | Outfit Planner | Vara | `feature/outfit-planner` |
| `feature/inspiration` | Style Inspiration Board | Sela | `feature/inspiration-board` |
| `feature/wishlist` | Fashion Wishlist | Gres | `feature/fashion-wishlist` |
| `feature/wearhistory` | Wear History | Aurel | `feature/wear-history` |

Authentication and Profile are shared features and are not included in these six allocations.

## Build on Windows

Install Android Studio, JDK 17 or newer, and Android SDK 36. Then run:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```

If the machine-wide Gradle directory is not writable:

```powershell
$env:GRADLE_USER_HOME = Join-Path $PWD '.gradle-user'
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```

## Optional Supabase configuration

Supabase is not required to launch the app. When cloud integration begins, create an ignored `secrets.properties` in the repository root:

```properties
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-publishable-or-anon-key
```

The same values may be supplied as Gradle properties or environment variables. Only a publishable/anon key is allowed in the Android client. Never use a service-role key.

`AppConfig` reads the generated values, `AppContainer` owns one lazy client, and `SupabaseProvider` installs Auth, PostgREST, and Storage. A blank configuration returns `null`; no connection occurs during application startup.

## Team workflow

Follow [TEAM_ONBOARDING.md](docs/TEAM_ONBOARDING.md). In short: work on the assigned existing feature branch, merge the latest `develop`, keep feature code inside the owned package, coordinate route changes, test, and open a Pull Request targeting `develop`.

Additional contracts:

- [Navigation](docs/NAVIGATION_CONTRACT.md)
- [Planned database](docs/DATABASE_CONTRACT.md)
- [Repository boundaries](docs/REPOSITORY_CONTRACTS.md)
- [Shared UI](docs/UI_GUIDELINES.md)
- [Remaining app-level work](docs/TODO.md)
