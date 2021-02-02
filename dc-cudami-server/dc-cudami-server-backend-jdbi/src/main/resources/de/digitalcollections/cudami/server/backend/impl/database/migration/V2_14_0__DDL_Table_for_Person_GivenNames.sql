DROP TABLE IF EXISTS rel_person_givennames;

CREATE TABLE IF NOT EXISTS person_givennames (
  person_uuid UUID NOT NULL,
  givenname_uuid UUID NOT NULL,
  sortIndex SMALLINT,

  PRIMARY KEY (person_uuid, givenname_uuid)
-- Does not work with inheritance tables that have child tables:
--   FOREIGN KEY (givenname_uuid) REFERENCES givennames(uuid)
--   FOREIGN KEY (person_uuid) REFERENCES persons(uuid),
);
