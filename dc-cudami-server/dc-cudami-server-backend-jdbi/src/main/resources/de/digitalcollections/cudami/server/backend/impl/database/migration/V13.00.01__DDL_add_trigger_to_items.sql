CREATE TRIGGER tr_items_holder_uuids_agents
BEFORE INSERT OR UPDATE
ON items
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('agents', 'holder_uuids');

