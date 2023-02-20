DROP TABLE IF EXISTS tags;

CREATE TABLE IF NOT EXISTS tags (
  uuid UUID PRIMARY KEY,
  value VARCHAR collate "ucs_basic",
  created TIMESTAMP NOT NULL,
  last_modified TIMESTAMP NOT NULL,
  UNIQUE (value)
);