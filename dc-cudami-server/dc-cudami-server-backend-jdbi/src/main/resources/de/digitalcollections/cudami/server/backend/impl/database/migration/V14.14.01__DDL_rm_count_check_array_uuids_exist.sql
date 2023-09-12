CREATE OR REPLACE FUNCTION public.check_array_uuids_exist()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
-- Triggers must pass (name of foreign table, column_name or column.property of uuid[] to check)!
DECLARE
  referenced_table CONSTANT TEXT := TG_ARGV[0];
  uuid_column_name CONSTANT TEXT := TG_ARGV[1];
  column_parameter_split varchar[];
  row_exists boolean := false;
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

        EXECUTE format($$select exists(select 1 from %I where uuid = $1)$$, referenced_table)
          INTO row_exists
          USING uuid_to_check;
        IF not row_exists THEN
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

    EXECUTE format($$select exists(select 1 from %I where uuid = $1)$$, referenced_table)
      INTO row_exists
      USING uuid_to_check;
    IF not row_exists THEN
      RAISE EXCEPTION 'In table % UUID % does not exist', referenced_table, uuid_to_check USING ERRCODE = 'foreign_key_violation';
    END IF;
  END LOOP;
  RETURN NEW;
END;
$function$
;
