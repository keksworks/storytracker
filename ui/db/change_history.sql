--changeset change_history
create table change_history(
  "table" varchar not null,
  rowId varchar not null,
  "column" varchar not null,
  oldValue varchar,
  newValue varchar,
  changedAt timestamptz not null default clock_timestamp(),
  changedBy bigint
);

--changeset change_history.idx
create index change_history_id_idx on change_history("table", rowId);

--changeset change_history:revoke-app-update
revoke update on change_history from app;

--changeset set_app_user onChange:RUN separator:none
create or replace procedure set_app_user(userId bigint) language plpgsql as $$
begin
  perform set_config('app.user', userId::text, true);
  update users set lastOnlineAt = current_timestamp where id = userId;
end; $$

--changeset get_app_user onChange:RUN separator:none
create or replace function get_app_user() returns bigint language plpgsql as $$
declare
  userId varchar;
begin
  userId := current_setting('app.user', true);
  return case when userId = '' then null else userId end;
end; $$

--changeset add_change_history onChange:RUN separator:none
create or replace function add_change_history() returns trigger language plpgsql as $$
declare
  col name;
  oldValue text;
  newValue text;
begin
  for col in (
    select attname from pg_catalog.pg_attribute a
      join pg_catalog.pg_class c on c.oid = a.attrelid and c.relname = tg_table_name
      join pg_catalog.pg_namespace n on n.oid = c.relnamespace and n.nspname = tg_table_schema
    where a.attnum > 0 and not a.attisdropped)
    loop
      execute format('select (($1).%I)::text', col) using new into newValue;
      execute format('select (($1).%I)::text', col) using old into oldValue;
      if (oldValue is not distinct from newValue) then continue; end if;

      insert into change_history ("table", rowId, "column", oldValue, newValue, changedBy) values (tg_table_name, new.id, col, oldValue, newValue, get_app_user());
    end loop;
  return new;
end; $$
