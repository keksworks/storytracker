--changeset projects
create table projects(
  ${id},
  name text,
  description text,
  iterationWeeks int not null default 1,
  startDay text not null default 'MONDAY',
  timezone text not null default 'UTC',
  version int not null default 0,
  iterations int not null default 0,
  bugsEstimatable boolean not null default false,
  velocityAveragedWeeks int not null default 3,
  updatedAt timestamptz not null default now(),
  createdAt timestamptz not null default now(),
  ${createdBy}
);

--changeset projects_history
create trigger projects_history after update on projects for each row execute function add_change_history();

--changeset projects.reviewTypes
alter table projects add column reviewTypes text[] not null default '{}';

--changeset projects.currentIterationNum
alter table projects rename column iterations to currentIterationNum;

--changeset projects.version:drop
alter table projects drop column version;
