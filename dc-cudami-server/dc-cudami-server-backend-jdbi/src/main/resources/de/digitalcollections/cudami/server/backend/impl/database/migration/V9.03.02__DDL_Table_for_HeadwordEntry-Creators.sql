CREATE TABLE IF NOT EXISTS headwordentry_creators (
  headwordentry_uuid UUID NOT NULL,
  agent_uuid UUID NOT NULL,
  sortIndex SMALLINT,

  PRIMARY KEY (headwordentry_uuid, agent_uuid)
--   Unfortunately not supported for tables with inheritance:
--   FOREIGN KEY (headwordentry_uuid) REFERENCES headwordentries(uuid),
--   FOREIGN KEY (agent_uuid) REFERENCES entities(uuid)
);
