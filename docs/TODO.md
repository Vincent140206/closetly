# Known TODOs and product decisions

## Before the database milestone

- Replace generic name/notes forms with complete feature-specific validated fields.
- Add date and currency pickers/formatting, wardrobe category selection, and outfit item selection.
- Add unit tests for ViewModels and fake repositories plus navigation/UI tests for every route family.
- Decide how dependency deletion is presented: block, archive, or explicit cascade preview.
- Add real image upload progress, retry, validation, and cleanup after Supabase Storage is configured.
- Add accessible component and screen previews.

## Inspiration decision

The proposal has two possible scopes: a private personal inspiration gallery, or community posts/bookmarks organized into collections. The current milestone implements the private grid and keeps `InspirationCollection` as an extension point. The group must choose before schema freeze. Do not add chat, follows, notifications, or a general social network.

## Planner to Wear History

When a completed plan creates a wear-history row, `source_plan_id` must be unique per user. `saveFromCompletedPlan` demonstrates the idempotency contract in memory; the database must enforce it with a unique index so repeated taps or retries cannot duplicate history.

## Authentication

The current demo repository is intentionally local and visibly labelled. Replace it with Supabase Auth without removing the repository boundary. Define session expiry, email verification, password reset, and account deletion behavior before production use.
