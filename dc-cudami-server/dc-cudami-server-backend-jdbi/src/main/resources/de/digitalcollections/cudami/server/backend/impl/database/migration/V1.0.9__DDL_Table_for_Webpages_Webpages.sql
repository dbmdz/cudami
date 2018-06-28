CREATE TABLE IF NOT EXISTS webpage_webpage (
  parent_webpage_uuid UUID NOT NULL,
  child_webpage_uuid UUID NOT NULL UNIQUE,

  PRIMARY KEY (parent_webpage_uuid, child_webpage_uuid),
  FOREIGN KEY (parent_webpage_uuid) REFERENCES webpages(uuid),
  FOREIGN KEY (child_webpage_uuid) REFERENCES webpages(uuid)
);
