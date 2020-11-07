CREATE TABLE IF NOT EXISTS article_creators (
  article_uuid UUID NOT NULL,
  agent_uuid UUID NOT NULL,
  sortIndex SMALLINT,

  PRIMARY KEY (article_uuid, agent_uuid)
--   FOREIGN KEY (article_uuid) REFERENCES articles(uuid),
--   FOREIGN KEY (agent_uuid) REFERENCES entities(uuid)
);
