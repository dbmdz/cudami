create table operations (
  id serial primary key,
  name varchar(45) not null
);

create table roles (
  id serial primary key,
  name varchar(45) not null
);

create table role_operation (
  role_id int not null references roles,
  operation_id int not null references operations
);

create table users (
  id serial primary key,
  email varchar(255) not null unique,
  enabled boolean default true,
  firstname varchar(255),
  lastname varchar(255),
  password varchar(255)
);

create table user_role (
  user_id int not null references users,
  role_id int not null references roles
);

