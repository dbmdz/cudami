CREATE TABLE IF NOT EXISTS project_digitalobjects (
  project_uuid UUID NOT NULL,
  digitalobject_uuid UUID NOT NULL,
  sortIndex INTEGER,

  PRIMARY KEY (project_uuid, digitalobject_uuid),
  FOREIGN KEY (project_uuid) REFERENCES projects(uuid),
  FOREIGN KEY (digitalobject_uuid) REFERENCES digitalobjects(uuid)
);
