CREATE TRIGGER tr_entities_tags
BEFORE INSERT OR UPDATE
ON entities
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist(tags, tags_uuids);

CREATE TRIGGER tr_familynames_tags
BEFORE INSERT OR UPDATE
ON familynames
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist(tags, tags_uuids);

CREATE TRIGGER tr_fileresources_tags
BEFORE INSERT OR UPDATE
ON fileresources
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist(tags, tags_uuids);

CREATE TRIGGER tr_givennames_tags
BEFORE INSERT OR UPDATE
ON givennames
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist(tags, tags_uuids);

CREATE TRIGGER tr_webpages_tags
BEFORE INSERT OR UPDATE
ON webpages
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist(tags, tags_uuids);

CREATE TRIGGER tr_agents_tags
BEFORE INSERT OR UPDATE
ON agents
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist(tags, tags_uuids);

CREATE TRIGGER tr_articles_tags
BEFORE INSERT OR UPDATE
ON articles
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist(tags, tags_uuids);

CREATE TRIGGER tr_collections_tags
BEFORE INSERT OR UPDATE
ON collections
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist(tags, tags_uuids);

CREATE TRIGGER tr_geolocations_tags
BEFORE INSERT OR UPDATE
ON geolocations
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist(tags, tags_uuids);

CREATE TRIGGER tr_projects_tags
BEFORE INSERT OR UPDATE
ON projects
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist(tags, tags_uuids);

CREATE TRIGGER tr_topics_tags
BEFORE INSERT OR UPDATE
ON topics
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist(tags, tags_uuids);

CREATE TRIGGER tr_websites_tags
BEFORE INSERT OR UPDATE
ON websites
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist(tags, tags_uuids);

CREATE TRIGGER tr_fileresources_application_tags
BEFORE INSERT OR UPDATE
ON fileresources_application
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist(tags, tags_uuids);

CREATE TRIGGER tr_fileresources_audio_tags
BEFORE INSERT OR UPDATE
ON fileresources_audio
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist(tags, tags_uuids);

CREATE TRIGGER tr_fileresources_image_tags
BEFORE INSERT OR UPDATE
ON fileresources_image
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist(tags, tags_uuids);

CREATE TRIGGER tr_fileresources_linkeddata_tags
BEFORE INSERT OR UPDATE
ON fileresources_linkeddata
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist(tags, tags_uuids);

CREATE TRIGGER tr_fileresources_text_tags
BEFORE INSERT OR UPDATE
ON fileresources_text
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist(tags, tags_uuids);

CREATE TRIGGER tr_fileresources_video_tags
BEFORE INSERT OR UPDATE
ON fileresources_video
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist(tags, tags_uuids);

CREATE TRIGGER tr_corporatebodies_tags
BEFORE INSERT OR UPDATE
ON corporatebodies
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist(tags, tags_uuids);

CREATE TRIGGER tr_persons_tags
BEFORE INSERT OR UPDATE
ON persons
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist(tags, tags_uuids);

CREATE TRIGGER tr_headwordentries_tags
BEFORE INSERT OR UPDATE
ON headwordentries
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist(tags, tags_uuids);

CREATE TRIGGER tr_humansettlements_tags
BEFORE INSERT OR UPDATE
ON humansettlements
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist(tags, tags_uuids);

CREATE TRIGGER tr_identifiables_tags
BEFORE INSERT OR UPDATE
ON identifiables
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist(tags, tags_uuids);

