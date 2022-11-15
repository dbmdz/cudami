CREATE OR REPLACE FUNCTION title_constructor(
  title_main_type varchar, title_sub_type varchar,
  titletext jsonb,
  locales varchar[]
)
RETURNS Title
LANGUAGE plpgsql AS
$body$
BEGIN
  RETURN ROW(ROW(title_main_type, title_sub_type)::mainsubtype, titletext, locales);
END;
$body$;

