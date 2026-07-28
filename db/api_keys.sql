--changeset api_keys
create table api_keys(
  ${id},
  userId bigint not null references users(id) on delete cascade,
  key text unique not null,
  name text not null default 'MCP',
  lastUsedAt timestamptz,
  createdAt timestamptz not null default now(),
  updatedAt timestamptz
);

--changeset api_keys:grant-delete
grant delete on api_keys to app;
