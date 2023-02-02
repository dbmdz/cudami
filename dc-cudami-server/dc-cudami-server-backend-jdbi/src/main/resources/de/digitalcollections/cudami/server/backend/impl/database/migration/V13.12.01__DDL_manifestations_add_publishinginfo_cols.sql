ALTER TABLE manifestations
  ADD COLUMN IF NOT EXISTS publishing_info_agent_uuids UUID[],
  ADD COLUMN IF NOT EXISTS publishing_info_locations_uuids UUID[];


CREATE TRIGGER tr_manifestations_publishing_info_agents
BEFORE INSERT OR UPDATE
ON manifestations
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('agents', 'publishing_info_agent_uuids');

CREATE TRIGGER tr_manifestations_publishing_info_locations
BEFORE INSERT OR UPDATE
ON manifestations
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('humansettlements', 'publishing_info_locations_uuids');


UPDATE manifestations SET publishing_info_agent_uuids = (
  SELECT array_agg((a->>'uuid')::UUID) FROM jsonb_path_query(
    jsonb_build_array(publication_info, production_info, distribution_info),
    '$[*].publishers[*].agent') agent(a))
  WHERE COALESCE(publication_info, production_info, distribution_info) IS NOT NULL;

UPDATE manifestations SET publishing_info_locations_uuids = (
  SELECT array_agg((a->>'uuid')::UUID) FROM jsonb_path_query(
    jsonb_build_array(publication_info, production_info, distribution_info),
    '$[*].publishers[*].locations[*]') humansettlement(a))
  WHERE COALESCE(publication_info, production_info, distribution_info) IS NOT NULL;

