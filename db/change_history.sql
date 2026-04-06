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

--changeset change_history.projectId
alter table change_history add column projectId bigint;

--changeset change_history.idx
create index change_history_id_idx on change_history("table", rowId);

--changeset change_history.rowId:bigint
drop index change_history_id_idx;
alter table change_history alter column rowId type bigint using rowId::bigint;
create index change_history_id_idx on change_history("table", rowId);

--changeset change_history:revoke-app-update
revoke update on change_history from app;

--changeset set_app_user onChange:RUN separator:none
create or replace procedure set_app_user(userId bigint) language plpgsql as $$
begin
  perform set_config('app.user', userId::text, true);
end; $$

--changeset get_app_user onChange:RUN separator:none
create or replace function get_app_user() returns bigint language plpgsql as $$
declare
  userId varchar;
begin
  userId := current_setting('app.user', true);
  return case when userId = '' then null else userId end;
end; $$

--changeset change_history_archive
alter table change_history rename to change_history_archive;

--changeset change_history:new
create table change_history(
  "table" varchar not null,
  rowId bigint not null,
  projectId bigint,
  old jsonb not null default '{}',
  new jsonb not null default '{}',
  changedAt timestamptz not null default now(),
  changedBy bigint default get_app_user()
);

--changeset change_history:migrate-from-archive
insert into change_history ("table", rowId, projectId, old, new, changedAt, changedBy)
select "table", rowId, projectId,
  coalesce(jsonb_object_agg("column", oldValue) filter (where oldValue is not null), '{}'::jsonb),
  jsonb_object_agg("column", newValue),
  min(changedAt), min(changedBy)
from change_history_archive
group by "table", rowId, projectId, date_trunc('second', changedAt), changedBy;

--changeset change_history_archive:drop context:TODO
drop table change_history_archive;

--changeset change_history_idx
create index change_history_idx on change_history("table", rowId);

--changeset change_history_project_idx
create index change_history_project_idx on change_history(projectId, changedAt desc);

--changeset add_change_history onChange:RUN separator:none
create or replace function add_change_history() returns trigger language plpgsql as $$
declare
  col text;
  oldValue text;
  newValue text;
  newRec jsonb := to_jsonb(new);
  oldRec jsonb := case when tg_op = 'INSERT' then '{}'::jsonb else to_jsonb(old) end;
  oldChanges jsonb := '{}'::jsonb;
  newChanges jsonb := '{}'::jsonb;
  pid bigint := case when tg_table_name = 'projects' then new.id else (newRec->>'projectid')::bigint end;
begin
  if (tg_op = 'UPDATE') then
    for col, newValue in select * from jsonb_each_text(newRec) loop
      if (col not in ('updatedat', 'projectid', 'id')) then
        oldValue := oldRec->>col;
        if (oldValue is distinct from newValue) then
          if (oldValue is not null) then
            oldChanges := oldChanges || jsonb_build_object(col, oldValue);
          end if;
          newChanges := newChanges || jsonb_build_object(col, newValue);
        end if;
      end if;
    end loop;
  end if;

  if (tg_op = 'INSERT' or newChanges <> '{}'::jsonb) then
    insert into change_history ("table", rowId, old, new, changedBy, projectId)
    values (tg_table_name, new.id, oldChanges, newChanges, get_app_user(), pid);
  end if;

  return new;
end; $$
