--changeset teacher_feedbacks
create table teacher_feedbacks(
  ${id},
  courseId bigint not null references courses(id),
  teacherId bigint not null references users(id),
  roomAndEquipment text,
  courseAndStudents text,
  studentsNumber text,
  supportType text,
  createdAt timestamptz not null default now()
  );

--changeset teacher_feedbacks.createdAt:drop
alter table teacher_feedbacks drop column createdAt;
