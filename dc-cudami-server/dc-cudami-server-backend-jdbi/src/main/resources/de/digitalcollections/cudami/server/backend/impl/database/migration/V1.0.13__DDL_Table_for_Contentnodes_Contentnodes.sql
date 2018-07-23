CREATE TABLE IF NOT EXISTS contentnode_contentnode (
  parent_contentnode_uuid UUID NOT NULL,
  child_contentnode_uuid UUID NOT NULL UNIQUE,

  PRIMARY KEY (parent_contentnode_uuid, child_contentnode_uuid),
  FOREIGN KEY (parent_contentnode_uuid) REFERENCES contentnodes(uuid),
  FOREIGN KEY (child_contentnode_uuid) REFERENCES contentnodes(uuid)
);
