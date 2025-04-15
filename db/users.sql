--changeset users
create table users(
  ${id},
  role text not null,
  firstName text not null,
  lastName text not null,
  email text unique not null,
  avatarUrl text,
  lang text not null default 'en',
  lastOnlineAt timestamptz,
  updatedAt timestamptz not null default now(),
  createdAt timestamptz not null default now(),
  ${createdBy}
);

--changeset users_history
create trigger users_history after update on users for each row execute function add_change_history();

--changeset users:demo-admin context:!prod onFail:MARK_RAN
insert into users (id, firstName, lastName, email, role) values (1, 'Demo', 'Admin', 'tk@codeborne.com', 'ADMIN');
