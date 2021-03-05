CREATE TABLE IF NOT EXISTS rel_identifiable_entities (
  identifiable_uuid UUID NOT NULL,
  entity_uuid UUID NOT NULL,
  sortIndex SMALLINT,

  PRIMARY KEY (identifiable_uuid, entity_uuid),
  FOREIGN KEY (identifiable_uuid) REFERENCES identifiables(uuid)
);
