CREATE OR REPLACE FUNCTION public.check_uuid_exists()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
-- Triggers must pass (name of foreign table, column name of uuid to check)!
DECLARE
  table_name CONSTANT TEXT := TG_ARGV[0];
  uuid_column_name CONSTANT TEXT := TG_ARGV[1];
  row_exists boolean := false;
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

  EXECUTE format($$select exists(select 1 from %I where uuid = $1)$$, table_name)
    INTO row_exists
    USING uuid_to_check;
  IF not row_exists THEN
    RAISE EXCEPTION 'In table % UUID % does not exist', table_name, uuid_to_check USING ERRCODE = 'foreign_key_violation';
  END IF;
  RETURN NEW;
END;
$function$
;
