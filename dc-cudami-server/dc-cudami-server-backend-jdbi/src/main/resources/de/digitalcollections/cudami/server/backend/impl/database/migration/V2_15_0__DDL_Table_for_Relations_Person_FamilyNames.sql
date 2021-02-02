DROP TABLE IF EXISTS rel_person_familynames;

CREATE TABLE IF NOT EXISTS person_familynames (
  person_uuid UUID NOT NULL,
  familyname_uuid UUID NOT NULL,
  sortIndex SMALLINT,

  PRIMARY KEY (person_uuid, familyname_uuid)
-- Does not work with inheritance tables that have child tables:
--   FOREIGN KEY (person_uuid) REFERENCES persons(uuid),
--   FOREIGN KEY (familyname_uuid) REFERENCES familynames(uuid)
);
