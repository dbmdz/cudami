ALTER TABLE fileresources_linkeddata ADD PRIMARY KEY(uuid);
CREATE TABLE IF NOT EXISTS digitalobject_linkeddataresources (
  digitalobject_uuid UUID NOT NULL,
  linkeddata_fileresource_uuid UUID NOT NULL,
  sortIndex SMALLINT,

  PRIMARY KEY (digitalobject_uuid, linkeddata_fileresource_uuid),
  FOREIGN KEY (digitalobject_uuid) REFERENCES digitalobjects(uuid),
  FOREIGN KEY (linkeddata_fileresource_uuid) REFERENCES fileresources_linkeddata(uuid)
);
