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
  table_name CONSTANT TEXT := TG_ARGV[0];
  uuid_column_name CONSTANT TEXT := TG_ARGV[1];
  row_count integer := 0;
  uuid_to_check UUID;
BEGIN
  IF table_name IS NULL OR uuid_column_name IS NULL THEN
    RAISE EXCEPTION 'Foreign table name (1) and name of uuid column to be checked (2) must be passed'
      USING ERRCODE = 'invalid_parameter_value';
  END IF;

  FOR uuid_to_check IN select a::UUID from json_array_elements_text(row_to_json(NEW)->uuid_column_name) as t(a) LOOP
    -- if there is not an UUID then we do not care and let the op go on
    CONTINUE WHEN uuid_to_check IS NULL;

    EXECUTE format($$SELECT count(*) FROM %I WHERE uuid = $1 $$, table_name)
      INTO row_count
      USING uuid_to_check;
    IF row_count < 1 THEN
      RAISE EXCEPTION 'In table % UUID % does not exist', table_name, uuid_to_check USING ERRCODE = 'foreign_key_violation';
    END IF;
  END LOOP;
  RETURN NEW;
END;
$func$
LANGUAGE plpgsql;

