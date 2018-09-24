CREATE TABLE IF NOT EXISTS article_identifiables (
  article_uuid UUID NOT NULL,
  identifiable_uuid UUID NOT NULL UNIQUE,
  sortIndex SMALLINT,

  PRIMARY KEY (article_uuid, identifiable_uuid),
  FOREIGN KEY (article_uuid) REFERENCES articles(uuid),
  FOREIGN KEY (identifiable_uuid) REFERENCES identifiables(uuid)
);
