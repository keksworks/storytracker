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

--changeset stories.afterId
alter table stories add foreign key (afterId) references stories(id);

--changeset stories.iteration
alter table stories add column iteration int,
  add foreign key (projectId, iteration) references iterations(projectId, number) on delete set null;

--changeset stories:reviews
alter table stories add column reviews jsonb not null default '[]';

--changeset stories_history
create trigger stories_history after update on stories for each row execute function add_change_history();

--changeset stories.ord
alter table stories add column ord double precision not null default 0;

--changeset stories:project-ord
create index on stories(projectId, ord);

--changeset iterations:drop-imported-future onChange:RUN
update stories s set iteration = null where iteration in (select number from iterations i where i.projectid = s.projectid and endDate > '2025-04-28');
delete from iterations where endDate > '2025-04-28';

--changeset stories.afterId:drop context:TODO
alter table stories drop column afterId;
