CREATE TABLE IF NOT EXISTS subjects (
  uuid UUID PRIMARY KEY,
  created timestamp NOT NULL,
  last_modified timestamp NOT NULL,
  label jsonb NOT NULL,
  identifiers dbIdentifier[]
);

