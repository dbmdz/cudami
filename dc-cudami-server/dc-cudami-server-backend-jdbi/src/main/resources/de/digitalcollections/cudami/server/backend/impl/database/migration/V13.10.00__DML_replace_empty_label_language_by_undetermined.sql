/*
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
  new_json jsonb;
BEGIN
  IF NOT $1 ? '' THEN
    RETURN $1;
  END IF;
  new_json := $1 || jsonb_build_object('und', ($1 ->> '') || (CASE WHEN $1 ->> 'und' IS NULL THEN '' ELSE ' ' || ($1 ->> 'und') END));
  new_json := new_json - '';
  RETURN new_json;
END;
$body$;

UPDATE identifiables SET "label" = replace_language("label") WHERE "label" ? '';

DROP FUNCTION replace_language;

