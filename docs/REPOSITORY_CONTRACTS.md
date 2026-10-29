# Repository boundaries

No feature repository interface or implementation exists on `develop`. This is intentional: repository APIs should follow actual feature use cases instead of a generic CRUD abstraction.

When implementing a feature:

- Define the repository interface inside the owned feature package.
- Keep Supabase DTO mapping and network/storage calls in that feature's data layer.
- Make the ViewModel depend on the interface, not directly on `SupabaseClient`.
- Add an in-memory fake only when it supports UI development or tests for that feature.
- Expose business operations with meaningful names rather than forcing every feature into one generic base repository.
- Scope all future Supabase operations to the authenticated user and rely on Row Level Security as the security boundary.

Register cross-feature or application-wide dependencies in `AppContainer` only when multiple consumers actually need them. A feature may provide its own factory until a shared dependency pattern becomes necessary.
