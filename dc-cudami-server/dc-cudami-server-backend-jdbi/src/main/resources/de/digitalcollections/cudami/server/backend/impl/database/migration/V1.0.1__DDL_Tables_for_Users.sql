CREATE TABLE users (
  id SERIAL PRIMARY KEY NOT NULL,
  uuid UUID NOT NULL UNIQUE,

  email VARCHAR NOT NULL UNIQUE,
  enabled BOOLEAN DEFAULT TRUE,
  firstname VARCHAR,
  lastname VARCHAR,
  passwordHash VARCHAR,
  roles VARCHAR[]
);
