CREATE TABLE IF NOT EXISTS topic_entities (
  topic_uuid UUID NOT NULL,
  entity_uuid UUID NOT NULL,
  sortIndex SMALLINT,

  PRIMARY KEY (topic_uuid, entity_uuid),
  FOREIGN KEY (topic_uuid) REFERENCES topics(uuid)
  -- FOREIGN KEY (entity_uuid) REFERENCES entities(uuid)
);
