/**
 * This can be a quite long running script due to updating the table identifiables.
 * Therefore it should already be run manually before being executed by flyway.
 *
 * Use `psql -1 -f this_file.sql ...` to run this script transactionally.
 */

CREATE OR REPLACE FUNCTION replace_language (jsonb) RETURNS jsonb
RETURNS NULL ON NULL INPUT
IMMUTABLE
PARALLEL SAFE
LANGUAGE plpgsql AS
$body$
DECLARE
  new_json jsonb := '{}';
  key text;
  json_entry jsonb;
  new_key text;
BEGIN
  FOR key IN select jsonb_object_keys($1) LOOP
    -- check the key and make changes where necessary
    IF key IN ('', 'und') THEN
      -- only once
      CONTINUE WHEN new_json ? 'und';
      /* being carful may lead to unwanted results
         anyhow it is better than losing data */
      json_entry := jsonb_build_object('und',
        trim(both from CASE WHEN $1 -> '' IS NOT NULL THEN $1 ->> '' ELSE '' END
          || CASE WHEN $1 -> 'und' IS NOT NULL THEN ' ' || ($1 ->> 'und') ELSE '' END));
    ELSIF key ~* '[_#]' THEN
      new_key := regexp_replace(key, '[_#]+', '-', 'ig');
      -- de_DE_#Latn would now be de-DE-Latn. Sadly we need de-Latn-DE though
      new_key := regexp_replace(new_key, '^(\w+)-(\w+)-(\w+)$', '\1-\3-\2');
      json_entry := jsonb_build_object(new_key, $1 -> key);
    ELSE
      -- take this entry as is
      json_entry := jsonb_build_object(key, $1 -> key);
    END IF;
    -- add the new part
    new_json := new_json || json_entry;
  END LOOP;
  RETURN new_json;
END;
$body$;


UPDATE identifiables SET "label" = replace_language("label") WHERE "label" ? '';

