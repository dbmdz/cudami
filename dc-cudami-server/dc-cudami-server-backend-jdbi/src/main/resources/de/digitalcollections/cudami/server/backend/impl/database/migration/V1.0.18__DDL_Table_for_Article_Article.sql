CREATE TABLE IF NOT EXISTS article_article (
  parent_article_uuid UUID NOT NULL,
  child_article_uuid UUID NOT NULL UNIQUE,
  sortIndex SMALLINT,

  PRIMARY KEY (parent_article_uuid, child_article_uuid),
  FOREIGN KEY (parent_article_uuid) REFERENCES articles(uuid),
  FOREIGN KEY (child_article_uuid) REFERENCES articles(uuid)
);
