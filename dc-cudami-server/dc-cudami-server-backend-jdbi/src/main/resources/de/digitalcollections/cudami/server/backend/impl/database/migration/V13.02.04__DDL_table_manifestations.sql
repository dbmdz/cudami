CREATE TABLE IF NOT EXISTS manifestations (
  LIKE entities INCLUDING ALL,
  composition varchar,
  dimensions varchar,
  expressionTypes MainSubType[],
  involvements_uuids UUID[],
  language varchar,
  manufacturingType varchar,
  mediaTypes varchar[],
  otherLanguages varchar[],
  parent UUID CONSTRAINT fk_parent REFERENCES manifestations (uuid),
  publications Publication[],
  publishingDatePresentation varchar,
  publishingDateRange daterange,
  scale varchar,
  series_uuids UUID[],
  sortKey varchar,
  subjects_uuids UUID[],
  titles Title[],
  version varchar,
  work UUID CONSTRAINT fk_work REFERENCES work
)
INHERITS entities;

-- TODO: Trigger for involvements, series, subjects ✓
-- TODO: create those tables

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

