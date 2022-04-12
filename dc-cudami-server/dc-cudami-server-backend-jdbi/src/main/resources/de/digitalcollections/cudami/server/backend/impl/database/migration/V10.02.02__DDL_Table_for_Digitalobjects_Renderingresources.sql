CREATE TABLE IF NOT EXISTS digitalobject_renderingresources (
  digitalobject_uuid UUID NOT NULL,
  fileresource_uuid UUID NOT NULL,
  sortIndex SMALLINT,

  PRIMARY KEY (digitalobject_uuid, fileresource_uuid),
  FOREIGN KEY (digitalobject_uuid) REFERENCES digitalobjects(uuid)
);
