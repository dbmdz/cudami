CREATE TABLE IF NOT EXISTS tags (
  uuid UUID PRIMARY KEY,
  label JSONB,
  namespace VARCHAR collate "ucs_basic",
  id VARCHAR COLLATE "ucs_basic",
  type VARCHAR collate "ucs_basic",
  created TIMESTAMP NOT NULL,
  last_modified TIMESTAMP NOT NULL,
  UNIQUE (type, namespace, id)
);