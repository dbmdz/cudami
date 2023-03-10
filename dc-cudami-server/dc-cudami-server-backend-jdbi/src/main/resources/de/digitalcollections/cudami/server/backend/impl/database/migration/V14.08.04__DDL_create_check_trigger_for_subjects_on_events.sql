CREATE TRIGGER tr_events_subjects
BEFORE INSERT OR UPDATE
ON events
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('subjects', 'subjects_uuids');

