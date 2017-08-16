CREATE TABLE IF NOT EXISTS entities (
  id SERIAL PRIMARY KEY NOT NULL,
  uuid UUID NOT NULL UNIQUE,

  created TIMESTAMP NOT NULL,
  description JSONB,
  entity_type VARCHAR NOT NULL,
  label JSONB,
  last_modified TIMESTAMP NOT NULL,
  thumbnail JSONB
);
