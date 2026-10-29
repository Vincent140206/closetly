# Team onboarding

## Start feature development

1. Clone the repository and open it in Android Studio.
2. Fetch remotes and checkout the existing branch assigned to your feature. Do not recreate it.
3. Merge the latest `develop` into your feature branch and resolve conflicts before adding functionality.
4. Work inside your owned `feature/<name>` package. Replace its `.gitkeep` with real source files when the first file is added.
5. Create the feature's models, repository interface, ViewModel, UI state, and screens based on its actual requirements.
6. Reuse shared theme and app-level UI primitives where they fit; keep feature-specific components in the feature package.
7. Coordinate new type-safe routes with the navigation maintainer before editing `Routes.kt` or `AppNavHost.kt`.
8. Add focused unit tests and, where practical, Compose/navigation tests.
9. Run `gradlew.bat :app:assembleDebug :app:testDebugUnitTest` on Windows.
10. Push your feature branch and create a Pull Request targeting `develop`.

## Ownership rules

- Do not implement or reorganize another member's feature without coordination.
- Do not add fake implementations for unowned modules to `develop`.
- Do not expose Supabase clients directly to Composables.
- Never commit `local.properties`, `secrets.properties`, `.env` files, keystores, or service-role keys.
- Keep shared changes narrowly scoped so other branches can merge them safely.
