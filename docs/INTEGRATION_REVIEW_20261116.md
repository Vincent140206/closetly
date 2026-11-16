# Branch integration review — 16 November 2026

Baseline: `develop` at `3e8dc738c66536729caace36c9cce4c574629c3f`. Remote branches were fetched from GitHub and compared by commit ancestry and file changes. Dates below are the latest commit dates in UTC+07, not GitHub push-event timestamps.

| Branch | Reviewed tip | Latest commit date | Result |
|---|---|---|---|
| `develop` | `3e8dc73` | 15 Nov | Integration base |
| `feature/digital-wardrobe` | `51e3c15` | 15 Nov | 20 new commits integrated |
| `feature/fashion-wishlist` | `f72bc46` | 14 Nov | New UI commit integrated; navigation conflict resolved |
| `feature/wear-history` | `c6bf1ca` | 16 Nov | New UI/read commit integrated; navigation conflict resolved |
| `feature/auth` | `eabec14` | 8 Nov | Already contained in develop |
| `feature/profile` | `eabec14` | 8 Nov | Already contained in develop |
| `feature/splash-screen` | `eabec14` | 8 Nov | Already contained in develop |
| `feature/home-dashboard` | `ce63007` | 14 Nov | Already contained in develop |
| `feature/inspiration-board` | `55108b0` | 8 Nov | Already contained in develop |
| `feature/mix-and-match` | `5843e31` | 9 Nov | Already contained in develop |
| `feature/outfit-planner` | `b082e20` | 15 Nov | Already contained in develop |
| `integration/feature-compat` | `dfc27c4` | 9 Nov | Already contained in develop |
| `main` | `21e24ef` | 15 Nov | Two main-only merge commits ahead, with the same source tree as the develop baseline |

Original feature commits and authorship are retained through merge commits. Feature branch tips are not rewritten. The integration checkout is separate from the user's working directory, including their IDE change and untracked SQL file.

## Integration fixes

- Keep the existing Wardrobe, Outfit, Inspiration, and Planner routes. The old Wishlist branch attempted to replace the Wardrobe route with a demo wrapper around a placeholder; Wishlist is instead connected through `WishlistRoute` and its collection tab opens the existing collection.
- Open Wear History through its actual destination, including the login prompt when no session/client is available. Destination-local state is scoped to the active account.
- Replace nested Wishlist filter dialogs with one white bottom sheet. Save demo entries with the rest of the restored navigation state so restoring a detail page does not discard newly created entries.
- Match the supplied Supabase inventory: `wear_history.date_worn` is nullable for pending entries and `is_worn` distinguishes completed history. Query completed entries and reject foreign-owner or malformed completed records before constructing calendar data.
- Guard Wear History reads against session changes, bound requests to 30 seconds, coalesce concurrent reads, preserve existing data after reload failures, and omit plan lookups when all history is manual.
- Update app-shell tests to exercise the new destinations rather than assert obsolete placeholders. Add pending-date, ownership, retry, request-coalescing, Wishlist restoration, and single-sheet regression coverage.

## Scope and remaining work

Wishlist remains a local demo. Saved-state restoration is not durable database persistence. Wear History reads Supabase, but create/update/delete are still the feature author's unfinished navigation flows; they report that no change was saved. This integration does not add those writes, implement private image delivery, or migrate Planner from local storage to Supabase.

The supplied CSV is a schema snapshot, not a live database verification on 16 November. No database migrations, RLS changes, or live account writes are performed here. No CI statuses or pull-request workflow runs were returned for the three source tips, and no tracked Actions workflow was present; local verification is required before merging.

## Validation

The combined source passed `:app:testDebugUnitTest :app:assembleDebug :app:lintDebug :app:assembleDebugAndroidTest` using the cached JDK 21 and Gradle dependencies. All 69 unit tests passed with zero failures/errors. Lint completed with zero errors and nine warnings covering SDK/dependency versions, modifier defaults, and resources. The Wear History screen also reports an existing deprecated icon API warning; no dependency upgrades or unrelated UI rewrite were added to silence it.

Android navigation, Wishlist sheet, and saved-state tests compiled into the test APK but were not executed because `adb devices` listed no device. Live authenticated Supabase reads/writes and two-account RLS checks were not run. Remote source heads and the develop baseline were rechecked before publication and still matched this inventory.
