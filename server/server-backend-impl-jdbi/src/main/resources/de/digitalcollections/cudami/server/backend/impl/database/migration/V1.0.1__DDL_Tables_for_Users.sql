create table users (
  id serial primary key,
  email varchar(255) not null unique,
  enabled boolean default true,
  firstname varchar(255),
  lastname varchar(255),
  password varchar(255),
  roles varchar(255)[]
);
