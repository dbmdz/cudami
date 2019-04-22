CREATE TABLE IF NOT EXISTS rel_entity_entities (
  subject_uuid UUID NOT NULL,
  predicate VARCHAR(512),
  object_uuid UUID NOT NULL,

  PRIMARY KEY (subject_uuid, predicate, object_uuid)
-- Does not work with inheritance tables that have child tables:
--   FOREIGN KEY (subject_uuid) REFERENCES entities(uuid),
--   FOREIGN KEY (object_uuid) REFERENCES entities(uuid)
);
