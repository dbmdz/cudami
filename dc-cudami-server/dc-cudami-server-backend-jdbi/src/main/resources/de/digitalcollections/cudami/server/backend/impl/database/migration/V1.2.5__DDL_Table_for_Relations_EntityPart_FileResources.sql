CREATE TABLE IF NOT EXISTS rel_entitypart_fileresources (
  entitypart_uuid UUID NOT NULL,
  fileresource_uuid UUID NOT NULL,
  sortIndex SMALLINT,

  PRIMARY KEY (entitypart_uuid, fileresource_uuid),
  FOREIGN KEY (fileresource_uuid) REFERENCES fileresources(uuid)
);
