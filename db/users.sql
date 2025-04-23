--changeset users
create table users(
  ${id},
  role text not null,
  name text not null,
  initials text not null,
  username text not null,
  email text unique not null,
  avatarUrl text,
  lang text not null default 'en',
  lastOnlineAt timestamptz,
  updatedAt timestamptz not null default now(),
  createdAt timestamptz not null default now(),
  ${createdBy}
);

--changeset users:nullable
alter table users alter column initials drop not null, alter column username drop not null;

--changeset users_history
create trigger users_history after update on users for each row execute function add_change_history();

--changeset users:codeborne-owners2
update users set role = 'OWNER' where email like '%@codeborne.com';
