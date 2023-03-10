CREATE TRIGGER tr_agents_prevent_removal
AFTER DELETE
ON agents
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('digitalobjects', 'creation_creator_uuid', 'involvements', 'agent_uuid', 'items', 'holder_uuids', 'manifestations', 'publishing_info_agent_uuids');

CREATE TRIGGER tr_entities_prevent_removal
AFTER DELETE
ON entities
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('rel_entity_entities', 'subject_uuid', 'rel_entity_entities', 'object_uuid', 'rel_entity_fileresources', 'entity_uuid', 'rel_identifiable_entities', 'entity_uuid', 'topic_entities', 'entity_uuid');

CREATE TRIGGER tr_fileresources_prevent_removal
AFTER DELETE
ON fileresources
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('digitalobject_fileresources', 'fileresource_uuid', 'digitalobject_renderingresources', 'fileresource_uuid', 'rel_entity_fileresources', 'fileresource_uuid', 'rel_identifiable_fileresources', 'fileresource_uuid', 'topic_fileresources', 'fileresource_uuid');

CREATE TRIGGER tr_geolocations_prevent_removal
AFTER DELETE
ON geolocations
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('digitalobjects', 'creation_geolocation_uuid');

CREATE TRIGGER tr_humansettlements_prevent_removal
AFTER DELETE
ON humansettlements
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('manifestations', 'publishing_info_locations_uuids');

CREATE TRIGGER tr_subjects_prevent_removal
AFTER DELETE
ON subjects
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('identifiables', 'subjects_uuids');

CREATE TRIGGER tr_tags_prevent_removal
AFTER DELETE
ON tags
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('identifiables', 'tags_uuids');

