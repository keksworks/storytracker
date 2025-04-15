--changeset invoices
create table invoices(
  ${id},
  enrollmentId bigint not null references enrollments(id),
  number text,
  type text not null default 'STANDARD',
  status text not null default 'ISSUED',
  date date not null default current_date,
  dueDate date not null,
  amount decimal not null,
  updatedAt timestamptz not null,
  ${createdBy}
);

--changeset invoices_history
create trigger invoices_history after update on invoices for each row execute function add_change_history();

--changeset invoices_updatedAt
alter table invoices alter column updatedAt set default now()

--changeset invoices.details
alter table invoices add column description text;

--changeset invoices_enrollment_idx
create index invoices_enrollment_idx on invoices (enrollmentId);

--changeset invoices_date_idx
create index invoices_date_idx on invoices (date);

--changeset invoices:fix-discount-null-in-descriptions onChange:RUN
update invoices set description = replace(replace(description, ' soodustus null%', ''), ' discount null%', '') where description like '% null%';

--changeset invoices.payer
alter table invoices add column payer text not null default 'SELF';

--changeset invoices.set-payer-from-enrollment
update invoices set payer = (select payer from enrollments where id = enrollmentId);

--changeset invoices.number:drop context:TODO
alter table invoices drop column number;
