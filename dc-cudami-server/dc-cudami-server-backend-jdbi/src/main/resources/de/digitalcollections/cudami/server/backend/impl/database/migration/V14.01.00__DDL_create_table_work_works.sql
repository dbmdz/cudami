CREATE TABLE IF NOT EXISTS work_works (
  subject_uuid UUID NOT NULL,
  object_uuid UUID NOT NULL

  PRIMARY KEY (subject_uuid, object_uuid),
  CONSTRAINT fk_subject_uuid FOREIGN KEY (subject_uuid) REFERENCES works(uuid),
  CONSTRAINT fk_object_uuid FOREIGN KEY (object_uuid) REFERENCES works(uuid)
);
