CREATE TABLE IF NOT EXISTS item_digitalobjects (
  item_uuid UUID NOT NULL,
  digitalobject_uuid UUID NOT NULL,
  sortIndex SMALLINT,

  PRIMARY KEY (item_uuid, digitalobject_uuid)
--   FOREIGN KEY (item_uuid) REFERENCES items(uuid),
--   FOREIGN KEY (digitalobject_uuid) REFERENCES digitalobjects(uuid)
);
