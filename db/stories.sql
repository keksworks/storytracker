--changeset stories
create table stories(
  ${id},
  projectId int not null references projects(id) on delete cascade,
  name text not null,
  description text,
  type text not null default 'FEATURE',
  status text not null default 'UNSTARTED',
  tags text[] not null default '{}',
  points int,
  externalId text,
  afterId bigint,
  tasks jsonb not null default '[]',
  comments jsonb not null default '[]',
  blockers jsonb not null default '[]',
  acceptedAt timestamptz,
  deadline date,
  updatedAt timestamptz not null default now(),
  createdAt timestamptz not null default now(),
  ${createdBy}
);

--changeset stories.afterId:index
create index on stories(afterId);

--changeset stories.afterId:index:drop
drop index if exists stories_afterId_idx;

--changeset stories:mainIndex
create index on stories(projectId, afterId);

--changeset stories:mainIndex:drop
drop index stories_projectId_afterId_idx;

--changeset stories:mainIndexUnique
alter table stories add constraint stories_project_after_uq unique (projectId, afterId) deferrable initially immediate;

--changeset stories.afterId
alter table stories add foreign key (afterId) references stories(id);

--changeset stories.iteration
alter table stories add column iteration int,
  add foreign key (projectId, iteration) references iterations(projectId, number) on delete set null;

--changeset stories:reviews
alter table stories add column reviews jsonb not null default '[]';

--changeset stories_history
create trigger stories_history after update on stories for each row execute function add_change_history();
