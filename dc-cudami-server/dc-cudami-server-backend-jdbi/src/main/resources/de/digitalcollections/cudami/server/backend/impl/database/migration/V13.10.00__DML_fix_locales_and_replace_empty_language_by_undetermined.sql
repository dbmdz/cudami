/**
 * This can be a quite long running script due to updating the table identifiables.
 * Therefore it should already be run manually before being executed by flyway.
 *
 * Use `psql -1 -f this_file.sql ...` to run this script transactionally.
 */

CREATE OR REPLACE FUNCTION convert_locale (text) RETURNS text
RETURNS NULL ON NULL INPUT
IMMUTABLE
PARALLEL SAFE
LANGUAGE plpgsql AS
$body$
DECLARE
  result text;
BEGIN
  result := regexp_replace($1, '[_#]+', '-', 'ig');
  -- de_DE_#Latn would now be de-DE-Latn. Sadly we need de-Latn-DE though
  RETURN regexp_replace(result, '^(\w+)-(\w+)-(\w+)$', '\1-\3-\2');
END;
$body$;

CREATE OR REPLACE FUNCTION replace_language (jsonb) RETURNS jsonb
RETURNS NULL ON NULL INPUT
IMMUTABLE
PARALLEL SAFE
LANGUAGE plpgsql AS
$body$
DECLARE
  e jsonb;
  new_json jsonb := '{}';
  key text;
  json_entry jsonb;
  new_key text;
  struct_content jsonb;
BEGIN
  IF jsonb_typeof($1) = 'array' THEN
    new_json := '[]';
    FOR e IN select value from jsonb_array_elements($1) a(value) LOOP
      new_json := new_json || replace_language(e);
    END LOOP;
    RETURN new_json;
  END IF;

  FOR key IN select jsonb_object_keys($1) LOOP
    -- check the key and make changes where necessary
    IF key IN ('', 'und') THEN
      -- only once
      CONTINUE WHEN new_json ? 'und';
      /* being careful may lead to unwanted results
         anyhow it is better than losing data */
      IF jsonb_typeof($1->key) = 'object' THEN
        -- structured content
        struct_content := $1->'';
        IF struct_content IS NOT NULL THEN
          IF $1->'und'->'content' IS NOT NULL THEN
            struct_content := jsonb_set(struct_content, '{content}', struct_content->'content' || ($1->'und'->'content'));
          END IF;
        ELSE
          struct_content := $1->'und';
        END IF;
        json_entry := jsonb_build_object('und', struct_content);

      ELSIF jsonb_typeof($1->key) = 'string' THEN
        json_entry := jsonb_build_object('und',
          trim(both from CASE WHEN $1 -> '' IS NOT NULL THEN $1 ->> '' ELSE '' END
            || CASE WHEN $1 -> 'und' IS NOT NULL THEN ' ' || ($1 ->> 'und') ELSE '' END));

      ELSE
        -- instead of destroying something we would just leave it unchanged if we met an unknown case
        json_entry := jsonb_build_object(key, $1 -> key);
      END IF;
    ELSIF key ~* '[_#]' THEN
      new_key := convert_locale(key);
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


-- Tests
DO $tests$
BEGIN
  ASSERT replace_language('{"de": "some text", "en": "some more text"}'::jsonb)
    = '{"de": "some text", "en": "some more text"}'::jsonb;
  ASSERT replace_language('{"de": "some long text", "und": "should be und", "und__#Latn": "must be und-Latn", "de_DE_#Latn": "will be shown as de-Latn-DE"}'::jsonb)
    = '{"de": "some long text", "und": "should be und", "und-Latn": "must be und-Latn", "de-Latn-DE": "will be shown as de-Latn-DE"}'::jsonb;
  ASSERT replace_language('{"de": "some long text", "": "must be und", "und": "should be appended to und", "und__#Latn": "must be und-Latn"}'::jsonb)
    = '{"de": "some long text", "und": "must be und should be appended to und", "und-Latn": "must be und-Latn"}'::jsonb;

  ASSERT replace_language('[{"": "some text"}, {"en__#Latn": "something", "de": "text"}]')
    = '[{"und": "some text"}, {"en-Latn": "something", "de": "text"}]'::jsonb;

  ASSERT replace_language(
      '[{"": {"type": "doc", "content": [{"type": "paragraph", "content": [{"text": "*TESTDATA*: note@type=desc mit Autor und Datum", "type": "text"}]}]}}]'::jsonb)
    = '[{"und": {"type": "doc", "content": [{"type": "paragraph", "content": [{"text": "*TESTDATA*: note@type=desc mit Autor und Datum", "type": "text"}]}]}}]'::jsonb;

  ASSERT replace_language(
      '{"": {"type": "doc", "content": [{"type": "paragraph", "content": [{"text": "*TESTDATA*: note@type=desc mit Autor und Datum", "type": "text"}]}]}, "und": {"type": "doc", "content": [{"type": "paragraph", "content": [{"text": "appended one", "type": "text"}]}]}}'::jsonb)
    = '{"und": {"type": "doc", "content": [{"type": "paragraph", "content": [{"text": "*TESTDATA*: note@type=desc mit Autor und Datum", "type": "text"}]}, {"type": "paragraph", "content": [{"text": "appended one", "type": "text"}]}]}}'::jsonb;

  ASSERT replace_language(
      '{"": {"type": "doc", "content": [{"type": "paragraph", "content": [{"text": "*TESTDATA*: note@type=desc mit Autor und Datum", "type": "text"}]}]}, "und": {"type": "doc", "content": [{"type": "paragraph", "content": [{"text": "appended one", "type": "text"}]}]}, "de": {"type": "doc", "content": [{"type": "paragraph", "content": [{"text": "german text", "type": "text"}]}]}}'::jsonb)
    = '{"und": {"type": "doc", "content": [{"type": "paragraph", "content": [{"text": "*TESTDATA*: note@type=desc mit Autor und Datum", "type": "text"}]}, {"type": "paragraph", "content": [{"text": "appended one", "type": "text"}]}]}, "de": {"type": "doc", "content": [{"type": "paragraph", "content": [{"text": "german text", "type": "text"}]}]}}'::jsonb;
END;
$tests$;


WITH ill_identifiables (uuid, kind) AS (
  SELECT uuid, 'label'
    FROM identifiables
    WHERE jsonb_path_exists("label", '$.keyvalue()[*] ? (@.key == "" || @.key like_regex "[_#]")')
  UNION ALL
  SELECT uuid, 'description'
    FROM identifiables
    WHERE jsonb_path_exists(description, '$.keyvalue()[*] ? (@.key == "" || @.key like_regex "[_#]")')
), update_labels AS (
  UPDATE identifiables SET "label" = replace_language("label") WHERE uuid in (SELECT uuid FROM ill_identifiables WHERE kind = 'label')
)
UPDATE identifiables SET description = replace_language(description) WHERE uuid in (SELECT uuid FROM ill_identifiables WHERE kind = 'description');

WITH ill_predicates (uuid, kind) AS (
  SELECT uuid, 'label'
    FROM predicates
    WHERE jsonb_path_exists("label", '$.keyvalue()[*] ? (@.key == "" || @.key like_regex "[_#]")')
  UNION ALL
  SELECT uuid, 'description'
    FROM predicates
    WHERE jsonb_path_exists(description, '$.keyvalue()[*] ? (@.key == "" || @.key like_regex "[_#]")')
), update_labels AS (
  UPDATE predicates SET "label" = replace_language("label") WHERE uuid in (SELECT uuid FROM ill_predicates WHERE kind = 'label')
)
UPDATE predicates SET description = replace_language(description) WHERE uuid in (SELECT uuid FROM ill_predicates WHERE kind = 'description');

UPDATE corporatebodies SET text = replace_language(text) WHERE jsonb_path_exists("text", '$.keyvalue()[*] ? (@.key == "" || @.key like_regex "[_#]")');
UPDATE articles SET text = replace_language(text) WHERE jsonb_path_exists("text", '$.keyvalue()[*] ? (@.key == "" || @.key like_regex "[_#]")');
UPDATE collections SET text = replace_language(text) WHERE jsonb_path_exists("text", '$.keyvalue()[*] ? (@.key == "" || @.key like_regex "[_#]")');
UPDATE projects SET text = replace_language(text) WHERE jsonb_path_exists("text", '$.keyvalue()[*] ? (@.key == "" || @.key like_regex "[_#]")');
UPDATE webpages SET text = replace_language(text) WHERE jsonb_path_exists("text", '$.keyvalue()[*] ? (@.key == "" || @.key like_regex "[_#]")');
UPDATE licenses SET "label" = replace_language("label") WHERE jsonb_path_exists("label", '$.keyvalue()[*] ? (@.key == "" || @.key like_regex "[_#]")');
UPDATE subjects SET "label" = replace_language("label") WHERE jsonb_path_exists("label", '$.keyvalue()[*] ? (@.key == "" || @.key like_regex "[_#]")');
UPDATE tags SET "label" = replace_language("label") WHERE jsonb_path_exists("label", '$.keyvalue()[*] ? (@.key == "" || @.key like_regex "[_#]")');

WITH ill_templates (uuid, kind) AS (
  SELECT uuid, 'label'
    FROM rendering_templates
    WHERE jsonb_path_exists("label", '$.keyvalue()[*] ? (@.key == "" || @.key like_regex "[_#]")')
  UNION ALL
  SELECT uuid, 'description'
    FROM rendering_templates
    WHERE jsonb_path_exists(description, '$.keyvalue()[*] ? (@.key == "" || @.key like_regex "[_#]")')
), update_labels AS (
  UPDATE rendering_templates SET "label" = replace_language("label") WHERE uuid in (SELECT uuid FROM ill_templates WHERE kind = 'label')
)
UPDATE rendering_templates SET description = replace_language(description) WHERE uuid in (SELECT uuid FROM ill_templates WHERE kind = 'description');

-- specials
-- first another function...w/o this would be even worse to read; we will remove it afterwards though
CREATE OR REPLACE FUNCTION convert_locale_array (varchar[]) RETURNS varchar[]
RETURNS NULL ON NULL INPUT
IMMUTABLE
PARALLEL SAFE
LANGUAGE plpgsql AS
$body$
DECLARE
  s varchar;
  new_array varchar[] := '{}';
BEGIN
  FOREACH s IN ARRAY $1 LOOP
    IF s = '' AND NOT ('und' = ANY (new_array)) THEN
      new_array := new_array || 'und';
    ELSIF s ~* '[_#]' THEN
      new_array := new_array || convert_locale(s);
    ELSE
      new_array := new_array || s;
    END IF;
  END LOOP;
  RETURN new_array;
END;
$body$;

UPDATE agents SET name = replace_language(name) WHERE jsonb_path_exists(name, '$.keyvalue()[*] ? (@.key == "" || @.key like_regex "[_#]")');
UPDATE agents SET name_locales_original_scripts = convert_locale_array(name_locales_original_scripts) WHERE name_locales_original_scripts IS NOT NULL;
UPDATE geolocations SET name = replace_language(name) WHERE jsonb_path_exists(name, '$.keyvalue()[*] ? (@.key == "" || @.key like_regex "[_#]")');
UPDATE geolocations SET name_locales_original_scripts = convert_locale_array(name_locales_original_scripts) WHERE name_locales_original_scripts IS NOT NULL;

DROP FUNCTION convert_locale_array;

UPDATE entities SET notes = replace_language(notes) WHERE jsonb_path_exists(notes, '$[*].keyvalue()[*] ? (@.key == "" || @.key like_regex "[_#]")');
UPDATE url_aliases SET target_language = convert_locale(target_language) WHERE target_language = '' OR target_language ~* '[_#]';

