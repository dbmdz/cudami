ALTER TABLE digitalobjects
  ADD CONSTRAINT fk_version FOREIGN KEY (version) REFERENCES versions,
  ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

CREATE TRIGGER tr_digitalobjects_creation_geolocation_uuid_geolocations
BEFORE INSERT OR UPDATE ON digitalobjects
FOR EACH ROW
EXECUTE FUNCTION check_uuid_exists('geolocations', 'creation_geolocation_uuid');

CREATE TRIGGER tr_digitalobjects_creation_creator_uuid_agents
BEFORE INSERT OR UPDATE ON digitalobjects
FOR EACH ROW
EXECUTE FUNCTION check_uuid_exists('agents', 'creation_creator_uuid');

