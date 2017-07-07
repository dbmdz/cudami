create table websites (
  id serial primary key,
  title varchar(255) not null,
  url varchar(255) not null unique
);
