CREATE TABLE users (
  id SERIAL primary key,
  email VARCHAR(255) not null unique,
  enabled BOOLEAN default true,
  firstname VARCHAR(255),
  lastname VARCHAR(255),
  passwordHash VARCHAR(255),
  roles VARCHAR(255)[],
  uuid UUID not null unique
);

CREATE INDEX email_idx ON users(email);
CREATE INDEX uuid_idx ON users(uuid);