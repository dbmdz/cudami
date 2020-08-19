CREATE TABLE IF NOT EXISTS persons (
  dateOfBirth date,
  timeValueOfBirth JSONB,
--   locationOfBirth UUID,
  dateOfDeath date,
  timeValueOfDeath JSONB,
--   locationOfDeath UUID,
  gender VARCHAR
) INHERITS (entities);
