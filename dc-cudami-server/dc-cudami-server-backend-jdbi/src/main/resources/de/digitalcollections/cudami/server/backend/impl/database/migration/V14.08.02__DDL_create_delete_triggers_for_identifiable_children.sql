CREATE TRIGGER tr_agents_identifiables_prevent_removal
AFTER DELETE
ON agents
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('identifiers', 'identifiable', 'rel_identifiable_entities', 'identifiable_uuid', 'rel_identifiable_fileresources', 'identifiable_uuid', 'url_aliases', 'target_uuid');

CREATE TRIGGER tr_articles_identifiables_prevent_removal
AFTER DELETE
ON articles
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('identifiers', 'identifiable', 'rel_identifiable_entities', 'identifiable_uuid', 'rel_identifiable_fileresources', 'identifiable_uuid', 'url_aliases', 'target_uuid');

CREATE TRIGGER tr_collections_identifiables_prevent_removal
AFTER DELETE
ON collections
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('identifiers', 'identifiable', 'rel_identifiable_entities', 'identifiable_uuid', 'rel_identifiable_fileresources', 'identifiable_uuid', 'url_aliases', 'target_uuid');

CREATE TRIGGER tr_corporatebodies_identifiables_prevent_removal
AFTER DELETE
ON corporatebodies
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('identifiers', 'identifiable', 'rel_identifiable_entities', 'identifiable_uuid', 'rel_identifiable_fileresources', 'identifiable_uuid', 'url_aliases', 'target_uuid');

CREATE TRIGGER tr_digitalobjects_identifiables_prevent_removal
AFTER DELETE
ON digitalobjects
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('identifiers', 'identifiable', 'rel_identifiable_entities', 'identifiable_uuid', 'rel_identifiable_fileresources', 'identifiable_uuid', 'url_aliases', 'target_uuid');

CREATE TRIGGER tr_entities_identifiables_prevent_removal
AFTER DELETE
ON entities
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('identifiers', 'identifiable', 'rel_identifiable_entities', 'identifiable_uuid', 'rel_identifiable_fileresources', 'identifiable_uuid', 'url_aliases', 'target_uuid');

CREATE TRIGGER tr_events_identifiables_prevent_removal
AFTER DELETE
ON events
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('identifiers', 'identifiable', 'rel_identifiable_entities', 'identifiable_uuid', 'rel_identifiable_fileresources', 'identifiable_uuid', 'url_aliases', 'target_uuid');

CREATE TRIGGER tr_familynames_identifiables_prevent_removal
AFTER DELETE
ON familynames
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('identifiers', 'identifiable', 'rel_identifiable_entities', 'identifiable_uuid', 'rel_identifiable_fileresources', 'identifiable_uuid', 'url_aliases', 'target_uuid');

CREATE TRIGGER tr_fileresources_identifiables_prevent_removal
AFTER DELETE
ON fileresources
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('identifiers', 'identifiable', 'rel_identifiable_entities', 'identifiable_uuid', 'rel_identifiable_fileresources', 'identifiable_uuid', 'url_aliases', 'target_uuid');

CREATE TRIGGER tr_fileresources_application_identifiables_prevent_removal
AFTER DELETE
ON fileresources_application
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('identifiers', 'identifiable', 'rel_identifiable_entities', 'identifiable_uuid', 'rel_identifiable_fileresources', 'identifiable_uuid', 'url_aliases', 'target_uuid');

CREATE TRIGGER tr_fileresources_audio_identifiables_prevent_removal
AFTER DELETE
ON fileresources_audio
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('identifiers', 'identifiable', 'rel_identifiable_entities', 'identifiable_uuid', 'rel_identifiable_fileresources', 'identifiable_uuid', 'url_aliases', 'target_uuid');

CREATE TRIGGER tr_fileresources_image_identifiables_prevent_removal
AFTER DELETE
ON fileresources_image
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('identifiers', 'identifiable', 'rel_identifiable_entities', 'identifiable_uuid', 'rel_identifiable_fileresources', 'identifiable_uuid', 'url_aliases', 'target_uuid');

CREATE TRIGGER tr_fileresources_linkeddata_identifiables_prevent_removal
AFTER DELETE
ON fileresources_linkeddata
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('identifiers', 'identifiable', 'rel_identifiable_entities', 'identifiable_uuid', 'rel_identifiable_fileresources', 'identifiable_uuid', 'url_aliases', 'target_uuid');

CREATE TRIGGER tr_fileresources_text_identifiables_prevent_removal
AFTER DELETE
ON fileresources_text
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('identifiers', 'identifiable', 'rel_identifiable_entities', 'identifiable_uuid', 'rel_identifiable_fileresources', 'identifiable_uuid', 'url_aliases', 'target_uuid');

CREATE TRIGGER tr_fileresources_video_identifiables_prevent_removal
AFTER DELETE
ON fileresources_video
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('identifiers', 'identifiable', 'rel_identifiable_entities', 'identifiable_uuid', 'rel_identifiable_fileresources', 'identifiable_uuid', 'url_aliases', 'target_uuid');

CREATE TRIGGER tr_geolocations_identifiables_prevent_removal
AFTER DELETE
ON geolocations
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('identifiers', 'identifiable', 'rel_identifiable_entities', 'identifiable_uuid', 'rel_identifiable_fileresources', 'identifiable_uuid', 'url_aliases', 'target_uuid');

CREATE TRIGGER tr_givennames_identifiables_prevent_removal
AFTER DELETE
ON givennames
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('identifiers', 'identifiable', 'rel_identifiable_entities', 'identifiable_uuid', 'rel_identifiable_fileresources', 'identifiable_uuid', 'url_aliases', 'target_uuid');

CREATE TRIGGER tr_headwordentries_identifiables_prevent_removal
AFTER DELETE
ON headwordentries
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('identifiers', 'identifiable', 'rel_identifiable_entities', 'identifiable_uuid', 'rel_identifiable_fileresources', 'identifiable_uuid', 'url_aliases', 'target_uuid');

CREATE TRIGGER tr_humansettlements_identifiables_prevent_removal
AFTER DELETE
ON humansettlements
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('identifiers', 'identifiable', 'rel_identifiable_entities', 'identifiable_uuid', 'rel_identifiable_fileresources', 'identifiable_uuid', 'url_aliases', 'target_uuid');

CREATE TRIGGER tr_identifiables_identifiables_prevent_removal
AFTER DELETE
ON identifiables
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('identifiers', 'identifiable', 'rel_identifiable_entities', 'identifiable_uuid', 'rel_identifiable_fileresources', 'identifiable_uuid', 'url_aliases', 'target_uuid');

CREATE TRIGGER tr_items_identifiables_prevent_removal
AFTER DELETE
ON items
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('identifiers', 'identifiable', 'rel_identifiable_entities', 'identifiable_uuid', 'rel_identifiable_fileresources', 'identifiable_uuid', 'url_aliases', 'target_uuid');

CREATE TRIGGER tr_manifestations_identifiables_prevent_removal
AFTER DELETE
ON manifestations
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('identifiers', 'identifiable', 'rel_identifiable_entities', 'identifiable_uuid', 'rel_identifiable_fileresources', 'identifiable_uuid', 'url_aliases', 'target_uuid');

CREATE TRIGGER tr_persons_identifiables_prevent_removal
AFTER DELETE
ON persons
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('identifiers', 'identifiable', 'rel_identifiable_entities', 'identifiable_uuid', 'rel_identifiable_fileresources', 'identifiable_uuid', 'url_aliases', 'target_uuid');

CREATE TRIGGER tr_projects_identifiables_prevent_removal
AFTER DELETE
ON projects
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('identifiers', 'identifiable', 'rel_identifiable_entities', 'identifiable_uuid', 'rel_identifiable_fileresources', 'identifiable_uuid', 'url_aliases', 'target_uuid');

CREATE TRIGGER tr_topics_identifiables_prevent_removal
AFTER DELETE
ON topics
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('identifiers', 'identifiable', 'rel_identifiable_entities', 'identifiable_uuid', 'rel_identifiable_fileresources', 'identifiable_uuid', 'url_aliases', 'target_uuid');

CREATE TRIGGER tr_webpages_identifiables_prevent_removal
AFTER DELETE
ON webpages
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('identifiers', 'identifiable', 'rel_identifiable_entities', 'identifiable_uuid', 'rel_identifiable_fileresources', 'identifiable_uuid', 'url_aliases', 'target_uuid');

CREATE TRIGGER tr_websites_identifiables_prevent_removal
AFTER DELETE
ON websites
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('identifiers', 'identifiable', 'rel_identifiable_entities', 'identifiable_uuid', 'rel_identifiable_fileresources', 'identifiable_uuid', 'url_aliases', 'target_uuid');

CREATE TRIGGER tr_works_identifiables_prevent_removal
AFTER DELETE
ON works
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('identifiers', 'identifiable', 'rel_identifiable_entities', 'identifiable_uuid', 'rel_identifiable_fileresources', 'identifiable_uuid', 'url_aliases', 'target_uuid');

