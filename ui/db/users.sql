--changeset users
create table users(
  ${id},
  role text not null,
  firstName text not null,
  lastName text not null,
  email text unique not null,
  phone text,
  avatarUrl text,
  personalCode text unique,
  comment text,
  ${createdBy}
);

--changeset users_history
create trigger users_history after update on users for each row execute function add_change_history();

--changeset users:demo-admin context:!prod onFail:MARK_RAN
insert into users (id, firstName, lastName, email, role) values (1, 'Demo', 'Admin', 'tk@codeborne.com', 'ADMIN');

--changeset users:demo-admin:personal-code context:!prod onChange:RUN
update users set personalCode = '30303039914' where email = 'tk@codeborne.com';

--changeset users:first-admins
insert into users (id, firstName, lastName, email, role) values
  (1000000, 'Elise', 'Jagomägi', 'elise.jagomagi@artun.ee', 'ADMIN'),
  (1000001, 'Marko', 'Metsoja', 'marko.metsoja@artun.ee', 'ADMIN');

--changeset users.lastOnlineAt
alter table users add column lastOnlineAt timestamptz;

--changeset users.remove_comment_column
alter table users drop column comment;

--changeset users.department
alter table users add column department text;

--changeset users.address
alter table users add column address jsonb;

--changeset users.updatedAt
alter table users add column updatedAt timestamptz;

--changeset users.updatedAt-define
update users set updatedAt = now() where updatedAt is null;

--changeset users.updatedAt-not-null
alter table users alter column updatedAt set not null, alter column updatedAt set default now();

--changeset users:demo_student context:!prod
insert into users (id, firstName, lastName, email, phone, role, personalCode, address)
  values (1000002, 'Demo', 'Student', 'demo@codeborne.com', '+372563636363', 'STUDENT', '49010220008', '{"id": "-1", "countryCode": "EE", "postalCode": "10101", "municipality": "Tallinn"}');

--changeset users.lang
alter table users add column lang text default 'et' not null;
