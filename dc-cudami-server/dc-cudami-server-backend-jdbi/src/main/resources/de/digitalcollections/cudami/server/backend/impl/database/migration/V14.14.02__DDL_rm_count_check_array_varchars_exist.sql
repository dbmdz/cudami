CREATE OR REPLACE FUNCTION public.check_array_varchars_exist()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
-- Triggers must pass (name of foreign table, foreign table's column, column_name or column.property of varchar[] to check)!
DECLARE
  referenced_table CONSTANT TEXT := TG_ARGV[0];
  referenced_column CONSTANT TEXT := TG_ARGV[1];
  varchar_column_name CONSTANT TEXT := TG_ARGV[2];
  column_parameter_split varchar[];
  row_exists boolean := false;
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

        EXECUTE format($$select exists(select 1 from %I where %I = $1)$$, referenced_table, referenced_column)
          INTO row_exists
          USING varchar_to_check;
        IF not row_exists THEN
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

    EXECUTE format($$select exists(select 1 from %I where %I = $1)$$, referenced_table, referenced_column)
      INTO row_exists
      USING varchar_to_check;
    IF not row_exists THEN
      RAISE EXCEPTION 'In table % the PK % does not exist', referenced_table, varchar_to_check USING ERRCODE = 'foreign_key_violation';
    END IF;
  END LOOP;
  RETURN NEW;
END;
$function$
;
