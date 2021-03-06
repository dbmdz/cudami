CREATE TABLE IF NOT EXISTS digitalobject_fileresources (
  digitalobject_uuid UUID NOT NULL,
  fileresource_uuid UUID NOT NULL UNIQUE,
  sortIndex SMALLINT,

  PRIMARY KEY (digitalobject_uuid, fileresource_uuid),
  FOREIGN KEY (digitalobject_uuid) REFERENCES digitalobjects(uuid),
  FOREIGN KEY (fileresource_uuid) REFERENCES fileresources(uuid)
);
