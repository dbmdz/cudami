/*
 * This is a trigger function that is quite similar to `check_array_uuids_exist()` except
 * that the checked "FK" in an array are varchar.
 *
 * :param 0: name of the referenced table
 * :param 1: column name of the referenced table (referenced PK)
 * :param 2: column with the varchar[] to check;
 *           can be a dotted string to a nested member
 *           (e.g. for columns of compound types), only one level deep;
 *           the column itself can be an array of compound types with varchar arrays too.
 *
 * Since array elements cannot reference a foreign table (FK constraint)
 * this function used by a trigger undertakes the necessary foreign key checks.
 */
CREATE OR REPLACE FUNCTION check_array_varchars_exist() RETURNS TRIGGER AS $func$
-- Triggers must pass (name of foreign table, foreign table's column, column_name or column.property of varchar[] to check)!
DECLARE
  referenced_table CONSTANT TEXT := TG_ARGV[0];
  referenced_column CONSTANT TEXT := TG_ARGV[1];
  varchar_column_name CONSTANT TEXT := TG_ARGV[2];
  column_parameter_split varchar[];
  row_count INTEGER := 0;
  varchar_arrayjs JSON;
  varchar_to_check varchar;
  part TEXT;
BEGIN
  IF referenced_table IS NULL OR referenced_column IS NULL OR varchar_column_name IS NULL THEN
    RAISE EXCEPTION 'Foreign table name (1) and column (2) and name of varchar[] column to be checked (3) must be passed'
      USING ERRCODE = 'invalid_parameter_value';
  END IF;

  varchar_arrayjs := row_to_json(NEW);
  -- even if there is nothing to split then an array with the one original string is returned
  column_parameter_split := regexp_split_to_array(varchar_column_name, '\.');
  varchar_arrayjs := varchar_arrayjs -> column_parameter_split[1];
  -- now we have the column value

  IF array_length(column_parameter_split, 1) = 2 THEN

    IF varchar_arrayjs IS NOT NULL AND json_typeof(varchar_arrayjs) = 'array' THEN
      -- special case: this column is an array of compound type --> go through this array and check the contained varchar arrays
      FOR varchar_to_check IN select (a #>> '{}')::varchar from jsonb_path_query(varchar_arrayjs::jsonb, format('$[*].%s[*]', column_parameter_split[2])::jsonpath) as t(a) LOOP
        -- if there is not a value then we do not care and let the op go on
        CONTINUE WHEN varchar_to_check IS NULL;

        EXECUTE format($$SELECT count(*) FROM %I WHERE %I = $1 $$, referenced_table, referenced_column)
          INTO row_count
          USING varchar_to_check;
        IF row_count < 1 THEN
          RAISE EXCEPTION 'In table % the PK % does not exist', referenced_table, varchar_to_check USING ERRCODE = 'foreign_key_violation';
        END IF;
      END LOOP;
      RETURN NEW;
    END IF;

    --usual case: compound_type.property
    varchar_arrayjs := varchar_arrayjs -> column_parameter_split[2];
  END IF;

  IF varchar_arrayjs IS NULL OR json_typeof(varchar_arrayjs) = 'null' THEN
    -- the column that could contain a varchar[] is NULL, nothing to do here
    -- (because of the JSON context this check might look strange)
    RETURN NEW;
  END IF;
  FOR varchar_to_check IN select a::varchar from json_array_elements_text(varchar_arrayjs) as t(a) LOOP
    -- if there is not a value then we do not care and let the op go on
    CONTINUE WHEN varchar_to_check IS NULL;

    EXECUTE format($$SELECT count(*) FROM %I WHERE %I = $1 $$, referenced_table, referenced_column)
      INTO row_count
      USING varchar_to_check;
    IF row_count < 1 THEN
      RAISE EXCEPTION 'In table % the PK % does not exist', referenced_table, varchar_to_check USING ERRCODE = 'foreign_key_violation';
    END IF;
  END LOOP;
  RETURN NEW;
END;
$func$
LANGUAGE plpgsql;

