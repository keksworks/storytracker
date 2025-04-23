--changeset iterations
create table iterations(
  projectId int not null references projects(id) on delete cascade,
  number int not null,
  length int not null default 1,
  teamStrength int not null default 100,
  startDate date not null,
  endDate date,
  points int,
  acceptedPoints int,
  velocity int,
  createdAt timestamptz not null default now(),
  primary key (projectId, number)
);
