--changeset courses
create table courses(
  ${id},
  accountingId int not null,
  code text not null,
  name jsonb not null default '{}',
  shortDescription jsonb not null default '{}',
  educationalGoals jsonb not null default '{}',
  evaluationCriteria jsonb not null default '{}',
  startDate date not null,
  endDate date not null,
  price decimal not null,
  preAcademyPrice decimal,
  status text not null default 'DRAFT',
  volume jsonb not null default '{}',
  curriculumGroup text not null,
  minParticipants int not null default 1,
  maxParticipants int,
  curriculumUrl text,
  moodleUrl text,
  responsibleEmail text,
  ${createdBy}
);

--changeset courses_history
create trigger courses_history after update on courses for each row execute function add_change_history();

--changeset courses:discount
alter table courses add discount_code text, add discount int;

--changeset courses:rename-discountcode
alter table courses rename column discount_code to discountcode;

--changeset courses:discountpercentage
alter table courses rename column discount to discountpercentage;
alter table courses alter column discountpercentage type decimal;

--changeset courses:teacherIds-coordinatorIds
alter table courses add teacherIds bigint[] not null default '{}', add coordinatorIds bigint[] not null default '{}';

--changeset courses.remove_preAcademyPrice
alter table courses drop column preAcademyPrice;

--changeset courses.updatedAt
alter table courses add column updatedAt timestamptz;

--changeset courses.updatedAt-define
update courses set updatedAt = now() where updatedAt is null;

--changeset courses.preAcademy
alter table courses add column preAcademy boolean;

--changeset courses.updatedAt-not-null
alter table courses alter column updatedAt set not null, alter column updatedAt set default now();

--changeset courses:discountCode-discountPercentage-registrationFee-scheduledPayments
alter table courses add column registrationFee decimal, add column scheduledPayments jsonb;

--changeset courses.remove_scheduledPayments
alter table courses drop column scheduledPayments;

--changeset courses.scheduledPayments
alter table courses add column scheduledPayments jsonb not null default '{}';

--changeset courses.remove_scheduledPayments_fix
alter table courses drop column scheduledPayments;

--changeset courses.scheduledPayments_fix
alter table courses add column scheduledPayments jsonb not null default '[]';

--changeset courses:demo_course context:!prod
insert into courses (id, accountingId, code, name, shortDescription, educationalGoals, evaluationCriteria, startDate, endDate, price, curriculumGroup, minParticipants, maxParticipants, volume) values
  (123988393, 100, 'DMM2025', '{"en": "Demo Course", "et": "Demo Kursus"}', '{"en": "Short description", "et": "Lühikirjeldus"}', '{"en": "Educational goals", "et": "Eesmärgid"}', '{"en": "Evaluation criteria", "et": "Kriteeriumid"}', '2024-09-14', '2025-05-10', 1000, 'design', 3, 10, '{"inClass": 20, "independent": 10}');

--changeset courses.type
alter table courses add column type text not null default 'MAIN';

--changeset courses.type:migrate
update courses set type = 'PRE_ACADEMY' where preAcademy;

--changeset courses.preAcademy:drop
alter table courses drop column preAcademy;

--changeset courses_start_date_idx
create index courses_start_date_idx on courses (startDate);

--changeset courses_end_date_idx
create index courses_end_date_idx on courses (endDate);

--changeset courses.discounts
alter table courses add column discounts jsonb not null default '[]';

--changeset courses.discounts:migrate onChange:MARK_RAN
update courses set discounts = ('[{"code": "' || discountCode || '", "percent": ' || discountPercentage || '}]')::jsonb where discountCode is not null and discountPercentage is not null;

--changeset courses.discountCode_discountPercentage:drop
alter table courses drop column discountCode, drop column discountPercentage;

--changeset courses.modules
alter table courses add column modules jsonb not null default '[]';

--changeset courses.applicationDocuments
alter table courses add column applicationDocuments text[] not null default '{}';

--changeset courses.attendanceMinPercentForCertificate
alter table courses add column attendanceMinPercentForCertificate decimal not null default 75.0;

--changeset courses.requiredDocuments
alter table courses rename column applicationDocuments to requiredDocuments;
