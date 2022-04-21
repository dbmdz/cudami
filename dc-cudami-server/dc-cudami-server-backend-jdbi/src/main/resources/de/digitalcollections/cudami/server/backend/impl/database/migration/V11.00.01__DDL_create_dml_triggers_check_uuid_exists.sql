/*
 * https://www.postgresql.org/docs/12/triggers.html
 * https://www.postgresql.org/docs/12/sql-createtrigger.html
 */

CREATE TRIGGER tr_digitalobject_renderingresources_fileresource_uuid 
BEFORE INSERT OR UPDATE ON digitalobject_renderingresources 
FOR EACH ROW 
EXECUTE FUNCTION check_uuid_exists('fileresources', 'fileresource_uuid');


CREATE TRIGGER tr_digitalobject_fileresources_fileresource_uuid 
BEFORE INSERT OR UPDATE ON digitalobject_fileresources 
FOR EACH ROW 
EXECUTE FUNCTION check_uuid_exists('fileresources', 'fileresource_uuid');


CREATE TRIGGER tr_identifiers_identifiable
BEFORE INSERT OR UPDATE ON identifiers
FOR EACH ROW 
EXECUTE FUNCTION check_uuid_exists('identifiables', 'identifiable');


CREATE TRIGGER tr_rel_entity_entities_subject_uuid
BEFORE INSERT OR UPDATE ON rel_entity_entities
FOR EACH ROW 
EXECUTE FUNCTION check_uuid_exists('entities', 'subject_uuid');

CREATE TRIGGER tr_rel_entity_entities_object_uuid
BEFORE INSERT OR UPDATE ON rel_entity_entities
FOR EACH ROW 
EXECUTE FUNCTION check_uuid_exists('entities', 'object_uuid');


CREATE TRIGGER tr_rel_entity_fileresources_fileresource_uuid 
BEFORE INSERT OR UPDATE ON rel_entity_fileresources 
FOR EACH ROW 
EXECUTE FUNCTION check_uuid_exists('fileresources', 'fileresource_uuid');

CREATE TRIGGER tr_rel_entity_fileresources_entity_uuid 
BEFORE INSERT OR UPDATE ON rel_entity_fileresources 
FOR EACH ROW 
EXECUTE FUNCTION check_uuid_exists('entities', 'entity_uuid');


CREATE TRIGGER tr_rel_identifiable_entities_identifiable_uuid
BEFORE INSERT OR UPDATE ON rel_identifiable_entities
FOR EACH ROW 
EXECUTE FUNCTION check_uuid_exists('identifiables', 'identifiable_uuid');

CREATE TRIGGER tr_rel_identifiable_entities_entity_uuid
BEFORE INSERT OR UPDATE ON rel_identifiable_entities
FOR EACH ROW 
EXECUTE FUNCTION check_uuid_exists('entities', 'entity_uuid');


CREATE TRIGGER tr_rel_identifiable_fileresources_fileresource_uuid 
BEFORE INSERT OR UPDATE ON rel_identifiable_fileresources 
FOR EACH ROW 
EXECUTE FUNCTION check_uuid_exists('fileresources', 'fileresource_uuid');

CREATE TRIGGER tr_rel_identifiable_fileresources_identifiable_uuid 
BEFORE INSERT OR UPDATE ON rel_identifiable_fileresources 
FOR EACH ROW 
EXECUTE FUNCTION check_uuid_exists('identifiables', 'identifiable_uuid');


CREATE TRIGGER tr_topic_entities_entity_uuid 
BEFORE INSERT OR UPDATE ON topic_entities
FOR EACH ROW 
EXECUTE FUNCTION check_uuid_exists('entities', 'entity_uuid');


CREATE TRIGGER tr_topic_fileresources_fileresource_uuid 
BEFORE INSERT OR UPDATE ON topic_fileresources 
FOR EACH ROW 
EXECUTE FUNCTION check_uuid_exists('fileresources', 'fileresource_uuid');


CREATE TRIGGER tr_url_aliases_target_uuid
BEFORE INSERT OR UPDATE ON url_aliases
FOR EACH ROW
EXECUTE FUNCTION check_uuid_exists('identifiables', 'target_uuid');
