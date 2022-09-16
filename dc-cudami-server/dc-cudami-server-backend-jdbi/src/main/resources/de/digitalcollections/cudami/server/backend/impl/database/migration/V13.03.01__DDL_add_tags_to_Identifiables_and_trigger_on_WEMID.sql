ALTER TABLE identifiables ADD COLUMN tags_uuids UUID[];

CREATE TRIGGER tr_digitalobjects_tags_uuids
BEFORE INSERT OR UPDATE
ON digitalobjects
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('tags', 'tags_uuids');

CREATE TRIGGER tr_items_tags_uuids
BEFORE INSERT OR UPDATE
ON items
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('tags', 'tags_uuids');

CREATE TRIGGER tr_digitalobjects_manifestations_uuids
BEFORE INSERT OR UPDATE
ON manifestations
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('tags', 'tags_uuids');

CREATE TRIGGER tr_works_tags_uuids
BEFORE INSERT OR UPDATE
ON works
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('tags', 'tags_uuids');