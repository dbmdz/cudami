ALTER TABLE identifiables ADD COLUMN IF NOT EXISTS split_label TEXT[];


CREATE FUNCTION label_splitting (lbl jsonb) RETURNS TEXT[]
IMMUTABLE
PARALLEL SAFE
RETURNS NULL ON NULL INPUT
LANGUAGE plpgsql AS $function$
DECLARE
  lbltext TEXT;
  split_hyphen_words TEXT[] = ARRAY[]::TEXT[];
  hw RECORD;
BEGIN
  SELECT INTO lbltext string_agg(lower(value), ' ') FROM jsonb_each_text(lbl);

  -- remove all special symbols and standalone hyphens
  lbltext := regexp_replace(lbltext, '[^[:space:]\w_-]|(?<=\s)-(?=\s)', '', 'g');

  -- split up words with hyphens additionally
  FOR hw IN SELECT regexp_matches(lbltext, '(\y\w+(-\w+)+\y)', 'g') AS rmtch LOOP
    split_hyphen_words := split_hyphen_words || regexp_split_to_array(trim(BOTH FROM hw.rmtch[1]), '-+');
  END LOOP;

  RETURN regexp_split_to_array(trim(BOTH FROM lbltext), '\s+') || split_hyphen_words;
END;
$function$;

UPDATE identifiables SET split_label = label_splitting("label") WHERE split_label IS NULL;

DROP FUNCTION label_splitting;

-- we only create indexes that are really necessary
CREATE INDEX IF NOT EXISTS idx_corporatebodies_split_label ON corporatebodies USING GIN (split_label);
CREATE INDEX IF NOT EXISTS idx_agents_split_label ON agents USING GIN (split_label);
CREATE INDEX IF NOT EXISTS idx_collections_split_label ON collections USING GIN (split_label);
CREATE INDEX IF NOT EXISTS idx_digitalobjects_split_label ON digitalobjects USING GIN (split_label);
CREATE INDEX IF NOT EXISTS idx_items_split_label ON items USING GIN (split_label);
CREATE INDEX IF NOT EXISTS idx_webpages_split_label ON webpages USING GIN (split_label);
CREATE INDEX IF NOT EXISTS idx_persons_split_label ON persons USING GIN (split_label);
CREATE INDEX IF NOT EXISTS idx_geolocations_split_label ON geolocations USING GIN (split_label);
CREATE INDEX IF NOT EXISTS idx_humansettlements_split_label ON humansettlements USING GIN (split_label);
CREATE INDEX IF NOT EXISTS idx_articles_split_label ON articles USING GIN (split_label);
CREATE INDEX IF NOT EXISTS idx_headwordentries_split_label ON headwordentries USING GIN (split_label);
CREATE INDEX IF NOT EXISTS idx_topics_split_label ON topics USING GIN (split_label);
CREATE INDEX IF NOT EXISTS idx_projects_split_label ON projects USING GIN (split_label);
CREATE INDEX IF NOT EXISTS idx_websites_split_label ON websites USING GIN (split_label);
CREATE INDEX IF NOT EXISTS idx_works_split_label ON works USING GIN (split_label);

ANALYZE;
