-- see V12.03.00

ALTER TABLE subjects ADD COLUMN IF NOT EXISTS split_label TEXT[];


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

UPDATE subjects SET split_label = label_splitting("label") WHERE split_label IS NULL;

DROP FUNCTION label_splitting;

-- we only create indexes that are really necessary, and this one shall be neccessary
CREATE INDEX IF NOT EXISTS idx_subjects_split_label ON subjects USING GIN (split_label);

ANALYZE;
