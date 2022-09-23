CREATE TABLE IF NOT EXISTS series (
  LIKE works INCLUDING ALL,
  manifestations MapTextToUuids[]
)
INHERITS (works);

CREATE TRIGGER tr_series_manifestations_uuid
BEFORE INSERT OR UPDATE ON series
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('manifestations', 'manifestations.uuids');

