ALTER TABLE identifiables ADD COLUMN IF NOT EXISTS subjects_uuids UUID[];

CREATE TRIGGER tr_agents_subjects
BEFORE INSERT OR UPDATE
ON agents
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('subjects', 'subjects_uuids');

CREATE TRIGGER tr_articles_subjects
BEFORE INSERT OR UPDATE
ON articles
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('subjects', 'subjects_uuids');

CREATE TRIGGER tr_collections_subjects
BEFORE INSERT OR UPDATE
ON collections
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('subjects', 'subjects_uuids');

CREATE TRIGGER tr_corporatebodies_subjects
BEFORE INSERT OR UPDATE
ON corporatebodies
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('subjects', 'subjects_uuids');

CREATE TRIGGER tr_digitalobjects_subjects
BEFORE INSERT OR UPDATE
ON digitalobjects
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('subjects', 'subjects_uuids');

CREATE TRIGGER tr_entities_subjects
BEFORE INSERT OR UPDATE
ON entities
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('subjects', 'subjects_uuids');

CREATE TRIGGER tr_familynames_subjects
BEFORE INSERT OR UPDATE
ON familynames
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('subjects', 'subjects_uuids');

CREATE TRIGGER tr_fileresources_subjects
BEFORE INSERT OR UPDATE
ON fileresources
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('subjects', 'subjects_uuids');

CREATE TRIGGER tr_fileresources_application_subjects
BEFORE INSERT OR UPDATE
ON fileresources_application
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('subjects', 'subjects_uuids');

CREATE TRIGGER tr_fileresources_audio_subjects
BEFORE INSERT OR UPDATE
ON fileresources_audio
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('subjects', 'subjects_uuids');

CREATE TRIGGER tr_fileresources_image_subjects
BEFORE INSERT OR UPDATE
ON fileresources_image
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('subjects', 'subjects_uuids');

CREATE TRIGGER tr_fileresources_linkeddata_subjects
BEFORE INSERT OR UPDATE
ON fileresources_linkeddata
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('subjects', 'subjects_uuids');

CREATE TRIGGER tr_fileresources_text_subjects
BEFORE INSERT OR UPDATE
ON fileresources_text
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('subjects', 'subjects_uuids');

CREATE TRIGGER tr_fileresources_video_subjects
BEFORE INSERT OR UPDATE
ON fileresources_video
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('subjects', 'subjects_uuids');

CREATE TRIGGER tr_geolocations_subjects
BEFORE INSERT OR UPDATE
ON geolocations
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('subjects', 'subjects_uuids');

CREATE TRIGGER tr_givennames_subjects
BEFORE INSERT OR UPDATE
ON givennames
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('subjects', 'subjects_uuids');

CREATE TRIGGER tr_headwordentries_subjects
BEFORE INSERT OR UPDATE
ON headwordentries
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('subjects', 'subjects_uuids');

CREATE TRIGGER tr_humansettlements_subjects
BEFORE INSERT OR UPDATE
ON humansettlements
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('subjects', 'subjects_uuids');

CREATE TRIGGER tr_identifiables_subjects
BEFORE INSERT OR UPDATE
ON identifiables
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('subjects', 'subjects_uuids');

CREATE TRIGGER tr_items_subjects
BEFORE INSERT OR UPDATE
ON items
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('subjects', 'subjects_uuids');

CREATE TRIGGER tr_persons_subjects
BEFORE INSERT OR UPDATE
ON persons
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('subjects', 'subjects_uuids');

CREATE TRIGGER tr_projects_subjects
BEFORE INSERT OR UPDATE
ON projects
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('subjects', 'subjects_uuids');

CREATE TRIGGER tr_topics_subjects
BEFORE INSERT OR UPDATE
ON topics
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('subjects', 'subjects_uuids');

CREATE TRIGGER tr_webpages_subjects
BEFORE INSERT OR UPDATE
ON webpages
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('subjects', 'subjects_uuids');

CREATE TRIGGER tr_websites_subjects
BEFORE INSERT OR UPDATE
ON websites
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('subjects', 'subjects_uuids');

