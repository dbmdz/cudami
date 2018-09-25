CREATE TABLE IF NOT EXISTS webpage_identifiables (
  webpage_uuid UUID NOT NULL,
  identifiable_uuid UUID NOT NULL UNIQUE,
  sortIndex SMALLINT,

  PRIMARY KEY (webpage_uuid, identifiable_uuid),
  FOREIGN KEY (webpage_uuid) REFERENCES webpages(uuid),
  FOREIGN KEY (identifiable_uuid) REFERENCES identifiables(uuid)
);
