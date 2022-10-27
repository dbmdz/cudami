CREATE TABLE IF NOT EXISTS manifestation_manifestations (
  subject_uuid UUID NOT NULL,
  object_uuid UUID NOT NULL,
  title varchar COLLATE "ucs_basic",
  sortKey varchar COLLATE "ucs_basic",

  PRIMARY KEY (subject_uuid, object_uuid),
  CONSTRAINT fk_subject_uuid FOREIGN KEY (subject_uuid) REFERENCES manifestations(uuid),
  CONSTRAINT fk_object_uuid FOREIGN KEY (object_uuid) REFERENCES manifestations(uuid)
);
