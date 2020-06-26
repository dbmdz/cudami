CREATE TABLE IF NOT EXISTS collection_collections (
  parent_collection_uuid UUID NOT NULL,
  child_collection_uuid UUID NOT NULL,
  sortIndex SMALLINT,

  PRIMARY KEY (parent_collection_uuid, child_collection_uuid),
  FOREIGN KEY (parent_collection_uuid) REFERENCES collections(uuid),
  FOREIGN KEY (child_collection_uuid) REFERENCES collections(uuid)
);
