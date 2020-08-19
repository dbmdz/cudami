CREATE TABLE IF NOT EXISTS item_works (
  item_uuid UUID NOT NULL,
  work_uuid UUID NOT NULL,
  sortIndex SMALLINT,

  PRIMARY KEY (item_uuid, work_uuid)
--   FOREIGN KEY (item_uuid) REFERENCES items(uuid),
--   FOREIGN KEY (work_uuid) REFERENCES works(uuid)
);
