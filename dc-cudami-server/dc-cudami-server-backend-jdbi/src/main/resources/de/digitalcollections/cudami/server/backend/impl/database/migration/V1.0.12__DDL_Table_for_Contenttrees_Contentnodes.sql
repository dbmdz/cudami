CREATE TABLE IF NOT EXISTS contenttree_contentnode (
  contenttree_uuid UUID NOT NULL,
  contentnode_uuid UUID NOT NULL UNIQUE,

  PRIMARY KEY (contenttree_uuid, contentnode_uuid),
  FOREIGN KEY (contenttree_uuid) REFERENCES contenttrees(uuid),
  FOREIGN KEY (contentnode_uuid) REFERENCES contentnodes(uuid)
);
