CREATE TABLE websites (
  id SERIAL primary key,
  url VARCHAR(255) not null unique,
  registration_date DATE,
  uuid UUID not null unique
);
