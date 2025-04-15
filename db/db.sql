--substitute id=id bigint primary key
--substitute createdBy=createdBy bigint default get_app_user()

--include app_user.sql
--include change_history.sql
--include users.sql
--include projects.sql
--include stories.sql
