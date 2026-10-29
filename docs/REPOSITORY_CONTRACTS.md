# Repository contracts

`CrudRepository<T>` exposes `observeAll`, `findById`, `save`, and `delete`. All feature repositories extend it with their own entity type. `InMemoryCrudRepository` is the UI-milestone implementation base and emits updates through `StateFlow`.

ViewModels depend on the feature interface, never on a fake class or Supabase. `AppContainer` owns repository instances and is the single replacement point for implementations.

## Cloud implementation rules

- Perform PostgREST and Storage work in repository implementations, never in Composables.
- Map database DTOs to UI/domain models in the data layer.
- Scope every operation to the authenticated user; RLS is mandatory and client filtering is not a security boundary.
- Represent recoverable failures in UI state and keep coroutine cancellation intact.
- Do not expose `SupabaseClient` to a screen or ViewModel.
- Prefer upsert only where conflict behavior is explicit; otherwise separate insert and update.
- Add feature-specific methods when the operation has business meaning, such as `saveFromCompletedPlan` in Wear History.

`ImageStorageRepository` accepts bytes and returns a stable storage path. Database rows store that path, not an expiring signed URL. Resolve a display URL at read time, then render it with Coil. Use paths shaped like `<user-id>/<feature>/<uuid>.<extension>` and validate type and size before upload.
