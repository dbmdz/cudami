CREATE TYPE MainSubType AS (
  mainType varchar COLLATE "ucs_basic",
  subType  varchar COLLATE "ucs_basic"
);

CREATE TYPE Title AS (
  titleType      MainSubType,
  title          jsonb,
  originalScript jsonb
);

CREATE TYPE Publication AS (
  locations               UUID[],
  publishers              UUID[],
  publishers_presentation varchar[] COLLATE "ucs_basic"
);

CREATE TYPE MapTextToUuids AS (
  key varchar COLLATE "ucs_basic",
  uuids UUID[]
);

