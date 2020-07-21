CREATE TABLE IF NOT EXISTS collection_digitalobjects (
  collection_uuid UUID NOT NULL,
  digitalobject_uuid UUID NOT NULL,
  sortIndex INTEGER,

  PRIMARY KEY (collection_uuid, digitalobject_uuid),
  FOREIGN KEY (collection_uuid) REFERENCES collections(uuid),
  FOREIGN KEY (digitalobject_uuid) REFERENCES digitalobjects(uuid)
);
