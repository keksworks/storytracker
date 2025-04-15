--changeset course_sessions
create table course_sessions(
  ${id},
  courseId bigint not null references courses(id),
  date date not null,
  startTime timetz not null,
  endTime timetz not null,
  location text,
  comment text,
  type text not null default 'LESSON',
  ${createdBy}
);

--changeset course_sessions:course_sessions_history
create trigger course_sessions_history after update on course_sessions for each row execute function add_change_history();

--changeset course_sessions:createdAt
alter table course_sessions add column updatedAt timestamptz not null default now();

--changeset course_sessions:startTime-endTime-type-time
alter table course_sessions alter column startTime type time;
alter table course_sessions alter column endTime type time;

--changeset course_sessions:isAssessment
alter table course_sessions drop column type;
alter table course_sessions add column isAssessment boolean not null default false;

--changeset course_sessions:location
update course_sessions set location = '?' where location is null;

--changeset course_sessions:location-not-null
alter table course_sessions alter column location set not null;

--changeset course_sessions:grant-delete-to-app-user
grant delete on course_sessions to app;

--changeset course_sessions_course_idx
create index course_sessions_course_idx on course_sessions (courseId);

--changeset course_sessions_course_idx:drop
drop index course_sessions_course_idx;

--changeset course_attendances:delete-duplicate-sessions onFail:MARK_RAN
delete from course_attendances where sessionId in (
  select id from course_sessions where (courseid, date, starttime) in (select courseid, date, starttime from course_sessions group by courseid, date, starttime having count(*) > 1)
    and id not in (select max(id) from course_sessions group by courseid, date, starttime)
);

--changeset course_sessions:delete-duplicates
delete from course_sessions where id in (
  select id from course_sessions where (courseid, date, starttime) in (select courseid, date, starttime from course_sessions group by courseid, date, starttime having count(*) > 1)
    and id not in (select max(id) from course_sessions group by courseid, date, starttime)
);

--changeset course_sessions_course_date_time_uq
create unique index course_sessions_course_date_time_uq on course_sessions (courseId, date, startTime);

--changeset course_sessions.module-code
alter table course_sessions add column moduleCode text;
