# Database contract (planned Supabase milestone)

This is a schema contract, not an applied migration. Use PostgreSQL `uuid` primary keys generated server-side, `timestamptz` audit columns, and `auth.users(id)` as the ownership source. Every user-owned row has a non-null `user_id` even when ownership can be inferred through a parent; the redundancy makes RLS and indexing explicit.

## Tables

### `profiles`

- `id uuid primary key references auth.users(id) on delete cascade`
- `display_name text not null`, `avatar_path text`, `created_at`, `updated_at`

### `clothing_items`

- `id uuid primary key`, `user_id uuid not null references profiles(id)`
- `name`, `color`, `size`, `category` as required text
- `image_path text`, `created_at`, `updated_at`, optional `archived_at`

### `outfits`

- `id uuid primary key`, `user_id uuid not null references profiles(id)`
- `name text not null`, `description text`, `cover_image_path text`, audit columns

### `outfit_items`

- `id uuid primary key`, `user_id uuid not null references profiles(id)`
- `outfit_id uuid not null references outfits(id) on delete cascade`
- `clothing_item_id uuid not null references clothing_items(id) on delete restrict`
- `position integer not null`, optional `role text`
- unique `(outfit_id, clothing_item_id, position)`

### `outfit_plans`

- `id uuid primary key`, `user_id uuid not null references profiles(id)`
- `outfit_id uuid not null references outfits(id) on delete restrict`
- `planned_date date not null`, `activity text not null`, `notes text`
- `status text not null` constrained to `planned`, `completed`, or `cancelled`
- audit columns

### `style_inspirations`

- `id uuid primary key`, `user_id uuid not null references profiles(id)`
- `title text not null`, `notes text`, `image_path text not null`
- optional `collection_id uuid`; add a separate `inspiration_collections` table if the group selects collections
- audit columns

If the community option is selected, add separate post/bookmark concepts after proposal approval. Do not overload ownership or RLS semantics of private inspiration rows.

### `fashion_wishlist`

- `id uuid primary key`, `user_id uuid not null references profiles(id)`
- `name`, `category` required text; `estimated_price numeric(12,2)` with non-negative check
- `store text`, `product_url text`, `image_path text`, `priority text`
- `is_purchased boolean not null default false`, audit columns

### `wear_history`

- `id uuid primary key`, `user_id uuid not null references profiles(id)`
- `outfit_id uuid not null references outfits(id) on delete restrict`
- `date_worn date not null`, `notes text`, `source_plan_id uuid references outfit_plans(id) on delete set null`
- audit columns
- a partial unique index on `(user_id, source_plan_id)` where `source_plan_id is not null`

## Row Level Security

Enable RLS on every public table. Authenticated users may select, insert, update, and delete only rows where `user_id = auth.uid()`. `profiles` uses `id = auth.uid()`. Child inserts/updates must additionally verify referenced outfit, clothing, or plan rows belong to the same `auth.uid()`; foreign keys alone do not enforce equal ownership.

Storage should use a private bucket such as `closetly-images`. Policies must restrict objects to a first path segment equal to the authenticated user's UUID. Use signed URLs or authenticated downloads for display. Never expose a service-role key in Android.

## Reference and deletion behavior

- A clothing item referenced by `outfit_items` must not be hard-deleted. Offer archive, show the blocking outfits, or require users to remove those references first.
- An outfit referenced by a plan or wear-history entry must not be hard-deleted. Prefer archive so history remains meaningful.
- Deleting an outfit may cascade only its join rows when no planner/history reference exists.
- Deleting a planner row sets `wear_history.source_plan_id` to null but must not delete the historical record.
- Profile/account deletion is a deliberate account-level operation and may cascade user data after confirmation and Storage cleanup.

Repositories must translate database constraint failures into clear user-facing conflicts. Do not silently cascade through cross-feature records.

## Indexes and timestamps

Index each `user_id`, all foreign keys, `outfit_plans(user_id, planned_date)`, `wear_history(user_id, date_worn desc)`, and useful category/status filters. Maintain `updated_at` with a database trigger rather than trusting the client clock.
