ALTER TABLE works
  RENAME date_published TO first_appeared_date;
ALTER TABLE works
  RENAME timevalue_published TO first_appeared_timevalue;
ALTER TABLE works
  ADD COLUMN IF NOT EXISTS first_appeared_presentation varchar COLLATE "ucs_basic",
  ADD COLUMN IF NOT EXISTS creation_daterange jsonb,
  ADD COLUMN IF NOT EXISTS creation_timevalue jsonb,
  ADD COLUMN IF NOT EXISTS subjects_uuids UUID[],
  ADD COLUMN IF NOT EXISTS titles Title[] NOT NULL


FK preview_fileresources


CREATE TRIGGER tr_works_subjects
BEFORE INSERT OR UPDATE ON works
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('subjects', 'subjects_uuids');