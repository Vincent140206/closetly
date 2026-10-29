# Closetly

Closetly is a Kotlin and Jetpack Compose wardrobe-management app built as a six-person Mobile Application Programming final project. This repository currently targets the **UI milestone**: every feature is navigable and uses realistic in-memory data, while repository contracts isolate the later Supabase milestone.

## Current capabilities

- Clearly labelled demo entry; login, registration, profile, and edit-profile scaffolds
- Home hub for all six assigned features
- Type-safe Navigation Compose routes for every list, detail, and add/edit screen
- Per-feature `ViewModel`, immutable `StateFlow`, repository interface, and fake repository
- Working in-memory add, edit, and delete flows
- Android Photo Picker and local Coil preview in image-capable forms
- Supabase Auth, PostgREST, and Storage client factory that is inert without explicit configuration

Demo mode is not real authentication and data is reset when the application process restarts.

## Technology

- Kotlin 2.2.21, Gradle Kotlin DSL, and a version catalog
- Jetpack Compose with Material 3
- Navigation Compose type-safe routes and Kotlinx Serialization
- Lifecycle ViewModel, Coroutines, `StateFlow`, and `collectAsStateWithLifecycle`
- Coil 3
- Supabase Kotlin SDK (Auth, PostgREST, Storage) and Ktor Android engine
- Android Photo Picker
- minSdk 26, target/compile SDK 36

## Feature ownership

| Feature | Owner | Main package | Future table |
|---|---|---|---|
| Digital Wardrobe | Vincent | `feature/wardrobe` | `clothing_items` |
| Mix & Match Outfit | Janice | `feature/outfit` | `outfits`, `outfit_items` |
| Outfit Planner | Vara | `feature/planner` | `outfit_plans` |
| Style Inspiration | Sela | `feature/inspiration` | `style_inspirations` |
| Fashion Wishlist | Gres | `feature/wishlist` | `fashion_wishlist` |
| Wear History | Aurel | `feature/wearhistory` | `wear_history` |

Authentication and Profile are shared foundations and do not count as individual features.

## Project structure

```text
app/src/main/java/vin/pam/closetly/
├── MainActivity.kt
├── ClosetlyApp.kt
├── core/
│   ├── data/                 # CRUD and Supabase/storage contracts
│   ├── model/                # Shared display contract
│   ├── navigation/           # Serializable routes and AppNavHost
│   └── ui/components/        # Shared scaffolds, states, cards, forms
├── di/AppContainer.kt        # Manual dependency injection and VM factory
├── feature/
│   ├── auth/                 # Shared demo auth foundation
│   ├── profile/              # Shared profile foundation
│   ├── home/                 # Six-feature hub
│   ├── wardrobe/
│   ├── outfit/
│   ├── planner/
│   ├── inspiration/
│   ├── wishlist/
│   └── wearhistory/
└── ui/theme/
docs/
├── DATABASE_CONTRACT.md
├── NAVIGATION_CONTRACT.md
├── REPOSITORY_CONTRACTS.md
├── UI_GUIDELINES.md
└── TODO.md
```

Each individual feature currently keeps its model, repository, fake implementation, and ViewModel in a compact `*Contract.kt`, with UI in `*Screens.kt`. Split these into `data/`, `domain/`, and `ui/` subpackages when a feature grows; do not move unrelated teammates' files during feature work.

## Build and run on Windows

1. Install Android Studio with JDK 17 or newer and Android SDK 36.
2. Open this repository and allow Gradle sync to finish.
3. Keep SDK paths and credentials only in ignored local files. `local.properties` must never be committed.
4. Select an emulator or Android device running API 26+.
5. Run the `app` configuration, or from PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug
```

If the machine's global Gradle directory is not writable, use a project-local cache for that terminal session:

```powershell
$env:GRADLE_USER_HOME = Join-Path $PWD '.gradle-user'
.\gradlew.bat :app:assembleDebug
```

The `.gradle-user/` directory is ignored.

## Moving from dummy data to Supabase

1. Create a Supabase project only during the database milestone.
2. Apply the schema and Row Level Security policies described in [DATABASE_CONTRACT.md](docs/DATABASE_CONTRACT.md).
3. Put the project URL and **anon/publishable key only** in an ignored local configuration source. Never ship a service-role key.
4. Call `SupabaseProvider.createOrNull(url, anonKey)` in a production container. A blank configuration deliberately returns `null`.
5. Implement each existing repository interface using PostgREST. Keep model mapping in the data layer.
6. Implement `ImageStorageRepository` with a private Storage bucket and per-user paths.
7. Replace one fake binding at a time in `AppContainer`; no screen or ViewModel rewrite should be necessary.
8. Add loading, error, retry, offline, and destructive-action tests before enabling cloud mode by default.

## Team workflow

Suggested branch convention: `feature/<owner>-<short-topic>`, `fix/<short-topic>`, and `docs/<short-topic>`. Examples: `feature/vincent-wardrobe-filter` and `fix/planner-date-validation`. Create and switch branches manually; automation must not do it for you.

For each member:

1. Pull the shared `develop` baseline and manually create your feature branch.
2. Work primarily inside your assigned `feature/<name>` package.
3. Extend the existing model and repository interface before changing UI data access.
4. Keep Compose functions stateless where practical; send events to the feature ViewModel.
5. Preserve type-safe route IDs and add routes centrally in `Routes.kt` and `AppNavHost.kt` only when a new screen is required.
6. Use the shared components and theme; add a shared component only when at least two features need it.
7. Add fake data first, then tests, then the Supabase implementation during the database milestone.
8. Run `gradlew.bat :app:assembleDebug` and exercise list, detail, add/edit, delete, and back navigation before opening a merge request.

See the `docs/` directory for the exact navigation, database, repository, UI, and decision contracts.
