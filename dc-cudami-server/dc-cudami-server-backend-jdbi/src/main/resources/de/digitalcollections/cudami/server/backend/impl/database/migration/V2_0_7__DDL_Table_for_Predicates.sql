CREATE TABLE IF NOT EXISTS predicates (
  value VARCHAR NOT NULL,
  label JSONB,
  description JSONB,
  created TIMESTAMP NOT NULL,
  last_modified TIMESTAMP NOT NULL,

  PRIMARY KEY (value)
);