CREATE TABLE IF NOT EXISTS rel_identifiable_fileresources (
  identifiable_uuid UUID NOT NULL,
  fileresource_uuid UUID NOT NULL,
  sortIndex SMALLINT,

  PRIMARY KEY (identifiable_uuid, fileresource_uuid),
  FOREIGN KEY (identifiable_uuid) REFERENCES identifiables(uuid),
  FOREIGN KEY (fileresource_uuid) REFERENCES fileresources(uuid)
);
