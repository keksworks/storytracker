--changeset projects
create table projects(
  ${id},
  name text,
  description text,
  iterationWeeks int,
  startDay text,
  updatedAt timestamptz not null default now(),
  createdAt timestamptz not null default now(),
  ${createdBy}
);

--changeset projects_history
create trigger projects_history after update on projects for each row execute function add_change_history();
