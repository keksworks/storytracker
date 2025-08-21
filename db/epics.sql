--changeset epics
create table epics(
  ${id},
  projectId int not null references projects(id) on delete cascade,
  name text not null,
  description text,
  tag text not null,
  updatedAt timestamptz not null default now(),
  createdAt timestamptz not null default now(),
  ${createdBy}
);

--changeset epics:epics.comments
alter table epics add column comments jsonb default '[]' not null;

--changeset epics_project_tag_idx
create unique index on epics(projectId, tag);

--changeset epics_history
create trigger epics_history after update on epics for each row execute function add_change_history();

--changeset epics.projectId:bigint
alter table epics alter column projectId type bigint using projectId::bigint;
