/*
 * This is a trigger function that is quite similar to `check_uuid_exists()` except
 * that it checks all UUIDs of an array (UUID[]).
 *
 * Since array elements cannot reference a foreign table (FK constraint)
 * this function used by a trigger undertakes the necessary foreign key checks.
 */
CREATE OR REPLACE FUNCTION check_array_uuids_exist() RETURNS TRIGGER AS $func$
-- Triggers must pass (name of foreign table, column name of uuid to check)!
DECLARE
  referenced_table CONSTANT TEXT := TG_ARGV[0];
  uuid_column_name CONSTANT TEXT := TG_ARGV[1];
  row_count INTEGER := 0;
  array_column JSON;
  uuid_to_check UUID;
BEGIN
  IF referenced_table IS NULL OR uuid_column_name IS NULL THEN
    RAISE EXCEPTION 'Foreign table name (1) and name of uuid column to be checked (2) must be passed'
      USING ERRCODE = 'invalid_parameter_value';
  END IF;

  array_column := row_to_json(NEW)->uuid_column_name;
  IF json_typeof(array_column) = 'null' THEN
    -- the column that could contain an UUID[] is NULL, nothing to do here
    -- (because of the JSON context this check might look strange)
    RETURN NEW;
  END IF;
  FOR uuid_to_check IN select a::UUID from json_array_elements_text(array_column) as t(a) LOOP
    -- if there is not an UUID then we do not care and let the op go on
    CONTINUE WHEN uuid_to_check IS NULL;

    EXECUTE format($$SELECT count(*) FROM %I WHERE uuid = $1 $$, referenced_table)
      INTO row_count
      USING uuid_to_check;
    IF row_count < 1 THEN
      RAISE EXCEPTION 'In table % UUID % does not exist', referenced_table, uuid_to_check USING ERRCODE = 'foreign_key_violation';
    END IF;
  END LOOP;
  RETURN NEW;
END;
$func$
LANGUAGE plpgsql;

