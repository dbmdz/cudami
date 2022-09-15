CREATE TABLE IF NOT EXISTS tags (
  uuid UUID NOT NULL PRIMARY KEY,
  namespace VARCHAR collate "ucs_basic",
  id VARCHAR COLLATE "ucs_basic",
  tag_type VARCHAR collate "ucs_basic",
  created TIMESTAMP NOT NULL,
  last_modified TIMESTAMP NOT NULL,
  UNIQUE (tag_type, namespace, id)
);