/*
 * This is a trigger function that is quite similar to `check_uuid_exists()` except
 * that it checks all UUIDs of an array (UUID[]).
 *
 * :param 0: name of the referenced table
 * :param 1: column with the UUID[] to check;
 *           can be a dotted string to a nested member
 *           (e.g. for columns of compound types), only one level deep;
 *           the column itself can be an array of compound types with UUID arrays too.
 *
 * Since array elements cannot reference a foreign table (FK constraint)
 * this function used by a trigger undertakes the necessary foreign key checks.
 */
CREATE OR REPLACE FUNCTION check_array_uuids_exist() RETURNS TRIGGER AS $func$
-- Triggers must pass (name of foreign table, column_name or column.property of uuid[] to check)!
DECLARE
  referenced_table CONSTANT TEXT := TG_ARGV[0];
  uuid_column_name CONSTANT TEXT := TG_ARGV[1];
  column_parameter_split varchar[];
  row_count INTEGER := 0;
  uuid_arrayjs JSON;
  uuid_to_check UUID;
  part TEXT;
BEGIN
  IF referenced_table IS NULL OR uuid_column_name IS NULL THEN
    RAISE EXCEPTION 'Foreign table name (1) and name of uuid column to be checked (2) must be passed'
      USING ERRCODE = 'invalid_parameter_value';
  END IF;

  uuid_arrayjs := row_to_json(NEW);
  -- even if there is nothing to split then an array with the one original string is returned
  column_parameter_split := regexp_split_to_array(uuid_column_name, '\.');
  uuid_arrayjs := uuid_arrayjs -> column_parameter_split[1];
  -- now we have the column value

  IF array_length(column_parameter_split, 1) = 2 THEN

    IF uuid_arrayjs IS NOT NULL AND json_typeof(uuid_arrayjs) = 'array' THEN
      -- special case: this column is an array of compound type --> go through this array and check the contained UUID arrays
      FOR uuid_to_check IN select (a #>> '{}')::UUID from jsonb_path_query(uuid_arrayjs::jsonb, format('$[*].%s[*]', column_parameter_split[2])::jsonpath) as t(a) LOOP
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
    END IF;

    --usual case: compound_type.property
    uuid_arrayjs := uuid_arrayjs -> column_parameter_split[2];
  END IF;

  IF uuid_arrayjs IS NULL OR json_typeof(uuid_arrayjs) = 'null' THEN
    -- the column that could contain an UUID[] is NULL, nothing to do here
    -- (because of the JSON context this check might look strange)
    RETURN NEW;
  END IF;
  FOR uuid_to_check IN select a::UUID from json_array_elements_text(uuid_arrayjs) as t(a) LOOP
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

