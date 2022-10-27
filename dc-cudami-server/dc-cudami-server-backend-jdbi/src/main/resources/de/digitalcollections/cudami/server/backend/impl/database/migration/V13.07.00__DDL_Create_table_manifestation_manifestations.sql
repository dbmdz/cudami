CREATE TABLE IF NOT EXISTS manifestation_manifestations (
  subject_uuid UUID NOT NULL,
  object_uuid UUID NOT NULL,
  title varchar,
  sortKey varchar,

  PRIMARY KEY (subject_uuid, object_uuid),
  FOREIGN KEY (subject_uuid) REFERENCES manifestations(uuid),
  FOREIGN KEY (object_uuid) REFERENCES manifestations(uuid)
);
