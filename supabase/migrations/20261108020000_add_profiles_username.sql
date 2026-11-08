begin;

alter table public.profiles
    add column if not exists username text;

-- Populate usernames when the existing signup flow inserts a profile.
create or replace function public.closetly_set_profile_username()
returns trigger
language plpgsql
security definer
set search_path = ''
as $function$
begin
    select u.raw_user_meta_data ->> 'username'
    into new.username
    from auth.users as u
    where u.id = new.id
      and jsonb_typeof(u.raw_user_meta_data -> 'username') = 'string'
      and (u.raw_user_meta_data ->> 'username') collate "C" ~ '^[a-z0-9]+$';

    return new;
end;
$function$;

revoke execute on function public.closetly_set_profile_username()
    from public, anon, authenticated, service_role;

create or replace trigger closetly_set_profile_username
    before insert on public.profiles
    for each row
    when (new.username is null)
    execute function public.closetly_set_profile_username();

-- Backfill profiles for accounts that already supplied a valid username.
update public.profiles as p
set username = u.raw_user_meta_data ->> 'username'
from auth.users as u
where u.id = p.id
  and p.username is null
  and jsonb_typeof(u.raw_user_meta_data -> 'username') = 'string'
  and (u.raw_user_meta_data ->> 'username') collate "C" ~ '^[a-z0-9]+$';

commit;
