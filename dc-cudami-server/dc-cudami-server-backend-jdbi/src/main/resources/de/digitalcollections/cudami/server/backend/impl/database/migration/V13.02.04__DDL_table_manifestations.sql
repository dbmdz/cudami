CREATE TABLE IF NOT EXISTS manifestations (
  LIKE entities INCLUDING ALL,
  composition                varchar COLLATE "ucs_basic",
  dimensions                 varchar COLLATE "ucs_basic",
  expressionTypes            MainSubType[],
  involvements_uuids         UUID[],
  language                   varchar COLLATE "ucs_basic",
  manufacturingType          varchar COLLATE "ucs_basic",
  mediaTypes                 varchar[] COLLATE "ucs_basic",
  otherLanguages             varchar[] COLLATE "ucs_basic",
  parent                     UUID,
  publications               Publication[],
  publishingDatePresentation varchar COLLATE "ucs_basic",
  publishingDateRange        daterange,
  scale                      varchar COLLATE "ucs_basic",
  series_uuids               UUID[],
  sortKey                    varchar COLLATE "ucs_basic",
  subjects_uuids             UUID[],
  titles                     Title[] NOT NULL,
  version                    varchar COLLATE "ucs_basic",
  work                       UUID CONSTRAINT fk_work REFERENCES works
)
INHERITS (entities);

ALTER TABLE manifestations ADD CONSTRAINT fk_parent FOREIGN KEY (parent) REFERENCES manifestations;

CREATE TRIGGER tr_manifestations_involvements
BEFORE INSERT OR UPDATE ON manifestations
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('involvements', 'involvements_uuids');

CREATE TRIGGER tr_manifestations_series
BEFORE INSERT OR UPDATE ON manifestations
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('series', 'series_uuids');

CREATE TRIGGER tr_manifestations_subjects
BEFORE INSERT OR UPDATE ON manifestations
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('subjects', 'subjects_uuids');

CREATE TRIGGER tr_manifestations_publications_locations_humansettlements
BEFORE INSERT OR UPDATE ON manifestations
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('humansettlements', 'publications.locations');

CREATE TRIGGER tr_manifestations_publications_publishers_agents
BEFORE INSERT OR UPDATE ON manifestations
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('agents', 'publications.publishers');

