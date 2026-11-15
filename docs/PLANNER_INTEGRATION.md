# Outfit Planner

This merge keeps the original Planner layout, calendar, categories, outfit chooser, status choices, detail, and delete confirmation. The current scope is the original create/read/delete flow. Edit, cancellation, actual wear-date handling, and Planner/Wear History integration are deferred.

Planner now reads outfit choices from the same Mix & Match repository used by the app. Mix & Match currently uses its local development repository. Plans are saved on the device in a separate file for each account, with a development scope when no account is available. The page states that plans are saved on the device. No Planner request is sent to Supabase.

A stable form UUID prevents a repeated save from adding the same plan twice. A plan is published to the screen only after the local file has been committed atomically. Deletion updates the visible list after the file write succeeds. Corrupt or unavailable storage produces an error without replacing existing saved data. Both save and delete errors remain visible in the current screen, and controls become available for retry.

The Planner's existing `Direncanakan` and `Sudah Dipakai` labels are local UI data in this phase. They do not update Wear History. The existing History navigation remains assigned to the Wear History feature.

## Supabase follow-up

The user explicitly deferred Supabase integration. The supplied CSV inventory was inspected read-only. It confirms that `outfit_plans` has `activity`, nullable `notes`, and `status` constrained to `planned`, `completed`, or `cancelled`. It does not have the original screen's `activity_name`, `outfit_name`, `outfit_image_url`, or `category` columns. Active triggers call `private.sync_plan_wear_history` and `private.validate_plan_wear_transition`; `mark_wear_history_worn` is also present.

Future cloud integration must map this local UI model to the verified tables and existing transaction behavior. Do not send the local snapshot directly as a Supabase DTO. This change does not alter tables, policies, RPCs, or existing cloud rows, and does not import existing cloud plans into local storage.

## Validation

Debug build, lint, and 48 unit tests passed on 15 November 2026. Nine emulator tests passed, covering the existing app navigation, Mix & Match outfit selection, plan creation/detail/deletion, and visible error recovery when deleting from a corrupted snapshot. Repository tests cover restart persistence, repeated request IDs, concurrent writes, account ownership, and storage failures. The original layout and labels remain in PlannerScreen; the data models and local file operations were moved into small separate files.
