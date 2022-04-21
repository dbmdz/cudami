/*
 * FYI read the docs ;-)
 * https://www.postgresql.org/docs/12/plpgsql.html
 * https://www.postgresql.org/docs/12/plpgsql-trigger.html
 * https://www.postgresql.org/docs/12/sql-createfunction.html
 */
CREATE OR REPLACE FUNCTION check_uuid_exists() RETURNS TRIGGER AS $func$
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

  uuid_to_check := (row_to_json(NEW)->>uuid_column_name)::UUID;
  IF uuid_to_check IS NULL THEN
    -- we do not care and let the op go on
    RETURN NEW;
  END IF;

  EXECUTE format($$SELECT count(*) FROM %I WHERE uuid = $1 $$, table_name)
    INTO row_count
    USING uuid_to_check;
  IF row_count < 1 THEN
    RAISE EXCEPTION 'In table % UUID % does not exist', table_name, uuid_to_check USING ERRCODE = 'foreign_key_violation';
  END IF;
  RETURN NEW;
END;
$func$ LANGUAGE plpgsql;
