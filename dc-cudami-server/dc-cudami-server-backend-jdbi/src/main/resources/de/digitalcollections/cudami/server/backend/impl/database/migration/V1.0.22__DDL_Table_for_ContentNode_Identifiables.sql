CREATE TABLE IF NOT EXISTS contentnode_identifiables (
  contentnode_uuid UUID NOT NULL,
  identifiable_uuid UUID NOT NULL UNIQUE,
  sortIndex SMALLINT,

  PRIMARY KEY (contentnode_uuid, identifiable_uuid),
  FOREIGN KEY (contentnode_uuid) REFERENCES contentnodes(uuid),
  FOREIGN KEY (identifiable_uuid) REFERENCES identifiables(uuid)
);
