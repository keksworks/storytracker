--changeset course_attendances
create table course_attendances(
  ${id},
  sessionId bigint not null references course_sessions(id),
  userId bigint not null references users(id),
  status text not null,
  comment text,
  updatedAt timestamptz not null default now(),
  ${createdBy}
);

--changeset course_attendances_history
create trigger course_attendances_history after update on course_attendances for each row execute function add_change_history();

--changeset course_attendances_session_user_uq
create unique index course_attendances_session_user_uq on course_attendances(sessionId, userId);

--changeset course_attendances.updatedAt:drop
alter table course_attendances drop column updatedAt;

--changeset course_attendances.studentId
alter table course_attendances rename column userId to studentId;

--changeset course_attendances.assessment
alter table course_attendances add column assessment text;

--changeset course_attendances.assessment:migrate
update course_attendances set assessment = 'FAILED', status = 'PRESENT' where status = 'FAILED';
update course_attendances set assessment = 'PASSED', status = 'PRESENT' where status = 'PASSED';

--changeset course_attendances_session_idx
create index course_attendances_session_idx on course_attendances (sessionId);

--changeset course_attendances_student_idx
create index course_attendances_student_idx on course_attendances (studentId);
