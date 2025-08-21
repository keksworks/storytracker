--changeset project_members
create table project_members(
  ${id},
  projectId int not null references projects(id) on delete cascade,
  userId int not null references users(id) on delete cascade,
  role text not null default 'MEMBER',
  commentNotifications boolean not null default false,
  mentionNotifications boolean not null default false,
  lastViewedAt timestamptz,
  updatedAt timestamptz not null default now(),
  createdAt timestamptz not null default now(),
  ${createdBy}
);

--changeset project_members_idx
create unique index on project_members(projectId, userId);

--changeset project_members_history
create trigger project_members_history after update on project_members for each row execute function add_change_history();

--changeset project_members.projectId:bigint
alter table project_members alter column projectId type bigint using projectId::bigint;

--changeset project_members.userId:bigint
alter table project_members alter column userId type bigint using userId::bigint;
