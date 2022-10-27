CREATE TABLE IF NOT EXISTS publishers (
  uuid UUID PRIMARY KEY,
  created timestamp NOT NULL,
  last_modified timestamp NOT NULL,
  agent_uuid UUID CONSTRAINT fk_publisher_agent REFERENCES agents,
  location_uuids UUID[],
  publisherPresentation varchar COLLATE "ucs_basic"
);

CREATE TRIGGER tr_publishers_locations_uuids_humansettlements
BEFORE INSERT OR UPDATE
ON publishers
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('humansettlements', 'location_uuids');