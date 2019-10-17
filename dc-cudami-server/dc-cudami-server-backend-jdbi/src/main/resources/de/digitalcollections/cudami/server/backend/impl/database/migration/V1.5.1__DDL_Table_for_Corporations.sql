CREATE TABLE IF NOT EXISTS corporations (
  id SERIAL PRIMARY KEY NOT NULL,
  uuid UUID NOT NULL UNIQUE,

  text JSONB,
  homepage_url VARCHAR
) INHERITS (entities);
