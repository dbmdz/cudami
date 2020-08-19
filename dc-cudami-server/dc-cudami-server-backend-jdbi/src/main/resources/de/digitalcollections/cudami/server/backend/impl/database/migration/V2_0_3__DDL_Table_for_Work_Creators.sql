CREATE TABLE IF NOT EXISTS work_creators (
  work_uuid UUID NOT NULL,
  agent_uuid UUID NOT NULL,
  sortIndex SMALLINT,

  PRIMARY KEY (work_uuid, agent_uuid)
--   FOREIGN KEY (work_uuid) REFERENCES works(uuid),
--   FOREIGN KEY (agent_uuid) REFERENCES entities(uuid)
);
