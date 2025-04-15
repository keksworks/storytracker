--changeset enrollments
create table enrollments(
  ${id},
  courseId bigint not null references courses(id),
  userId bigint not null references users(id),
  status text not null default 'APPLIED',
  payee text not null default 'SELF',
  discountCode text,
  creadtedAt timestamptz not null,
  ${createdBy}
);

--changeset enrollments_history
create trigger enrollments_history after update on enrollments for each row execute function add_change_history();

--changeset enrollments.payer
alter table enrollments rename column payee to payer;

--changeset enrollments.createdAt
alter table enrollments rename column creadtedAt to createdAt;

--changeset enrollments.createdAt:drop
alter table enrollments drop column createdAt;

--changeset enrollments.updatedAt
alter table enrollments add column updatedAt timestamptz;

--changeset enrollments.updatedAt-not-null
alter table enrollments alter column updatedAt set not null, alter column updatedAt set default now();

--changeset enrollments.enrollments_user_course_uq
alter table enrollments add constraint enrollments_user_course_uq unique (courseId, userId);

--changeset enrollments.payerDetails
alter table enrollments add column payerName text,
                        add column payerCode text,
                        add column invoiceEmail text,
                        add column payerAddress jsonb;

--changeset enrollments.details
alter table enrollments add column details text;

--changeset enrollments.specialNeeds
alter table enrollments add column specialNeeds text;

--changeset enrollments.adminComment
alter table enrollments add column adminComment text;

--changeset enrollments.studentComment
alter table enrollments rename column details to studentComment;

--changeset enrollments:demo-enrollment context:!prod
insert into enrollments (id, courseId, userId, status, payer, studentComment)
  values (1000002, 123988393, 1000002, 'APPROVED', 'SELF', 'Very eager to participate');

--changeset enrollments.paymentMethod
alter table enrollments add column paymentMethod text default null;

--changeset enrollments.paymentMethod:FULL
update enrollments set paymentMethod = 'FULL' where paymentMethod is null;
alter table enrollments alter column paymentMethod set not null, alter column paymentMethod set default 'FULL';

--changeset enrollments.reminderSentAt
alter table enrollments add column reminderSentAt timestamptz;

--changeset enrollments.confirmationLetterNo
alter table enrollments add column confirmationLetterNo text;

--changeset enrollments.unemploymentFundAmount
alter table enrollments add column unemploymentFundAmount decimal;

--changeset enrollments.payerAmount
alter table enrollments rename column unemploymentFundAmount to payerAmount;

--changeset enrollments_course_idx
create index enrollments_course_idx on enrollments (courseId);

--changeset enrollments_user_idx
create index enrollments_user_idx on enrollments (userId);

--changeset enrollments.applicationDocuments
alter table enrollments add column applicationDocuments jsonb not null default '{}';

--changeset enrollments.documents
alter table enrollments rename column applicationDocuments to documents;
