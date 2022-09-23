CREATE TABLE IF NOT EXISTS involvements (
  uuid               UUID PRIMARY KEY,
  created            timestamp NOT NULL,
  last_modified      timestamp NOT NULL,
  agent_uuid         UUID NOT NULL,
  place_uuid         UUID CONSTRAINT fk_place REFERENCES humansettlements,
  roles              varchar[] COLLATE "ucs_basic" NOT NULL,
  roles_presentation varchar[] COLLATE "ucs_basic",
  creator            boolean DEFAULT FALSE
);

CREATE TRIGGER tr_involvements_agent_uuid
BEFORE INSERT OR UPDATE ON involvements
FOR EACH ROW
EXECUTE FUNCTION check_uuid_exists('agents', 'agent_uuid');

