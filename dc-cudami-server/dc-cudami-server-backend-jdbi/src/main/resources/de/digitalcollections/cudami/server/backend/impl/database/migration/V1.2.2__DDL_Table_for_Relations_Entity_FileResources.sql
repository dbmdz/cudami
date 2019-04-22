CREATE TABLE IF NOT EXISTS rel_entity_fileresources (
  entity_uuid UUID NOT NULL,
  fileresource_uuid UUID NOT NULL,
  sortIndex SMALLINT,

  PRIMARY KEY (entity_uuid, fileresource_uuid),
--   FOREIGN KEY (entity_uuid) REFERENCES entities(uuid),
  FOREIGN KEY (fileresource_uuid) REFERENCES fileresources(uuid)
);
