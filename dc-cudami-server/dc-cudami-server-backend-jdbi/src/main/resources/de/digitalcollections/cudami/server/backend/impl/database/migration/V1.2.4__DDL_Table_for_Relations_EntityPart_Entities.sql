CREATE TABLE IF NOT EXISTS rel_entitypart_entities (
  entitypart_uuid UUID NOT NULL,
  entity_uuid UUID NOT NULL,
  sortIndex SMALLINT,

  PRIMARY KEY (entitypart_uuid, entity_uuid)
-- due to inheritance foreign keys are not possible, yet
);
