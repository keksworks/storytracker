--changeset app-user onFail:MARK_RAN
create user app with password '${DB_APP_PASS}';
-- in Heroku, it needs to be created manually with read-write permissions

--changeset app-user-grants
grant usage on schema public to app;

--changeset app-user-default-grants
alter default privileges in schema public grant select, insert, update on tables to app;
alter default privileges in schema public grant usage, select on sequences to app;
alter default privileges in schema public grant execute on functions to app;

--changeset db_changelog:revoke-from-app
revoke select, insert, update on db_changelog from app;
