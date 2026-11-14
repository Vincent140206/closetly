-- Digital Wardrobe metadata, based on the schema inspection supplied on 2026-11-14.
-- Archiving is represented by clothing_items.archived_at.
-- An empty adjustments object uses the application's default photo settings.
begin;

alter table public.clothing_items
    add column if not exists style text not null default '',
    add column if not exists is_favorite boolean not null default false,
    add column if not exists original_image_path text,
    add column if not exists photo_adjustments jsonb not null default '{}'::jsonb;

commit;
