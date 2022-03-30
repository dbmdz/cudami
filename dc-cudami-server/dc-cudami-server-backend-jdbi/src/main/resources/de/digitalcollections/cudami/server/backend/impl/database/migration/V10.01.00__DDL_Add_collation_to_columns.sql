/*
 * All varchar/text columns should be defined with an additional `COLLATE` statement
 * to make sure that we get proper Unicode collation that can handle special characters as well.
 * This is necessary for any `ORDER BY` and other internal sortings (e.g. indexes).
 */
ALTER TABLE entities ALTER COLUMN entity_type TYPE varchar COLLATE "ucs_basic";

ALTER TABLE identifiables ALTER COLUMN identifiable_type TYPE varchar COLLATE "ucs_basic";

ALTER TABLE corporatebodies ALTER COLUMN homepage_url TYPE varchar COLLATE "ucs_basic";

ALTER TABLE geolocations ALTER COLUMN geolocation_type TYPE varchar COLLATE "ucs_basic";

ALTER TABLE items ALTER COLUMN "language" TYPE varchar COLLATE "ucs_basic",
  ALTER COLUMN publication_date TYPE varchar COLLATE "ucs_basic",
  ALTER COLUMN publication_place TYPE varchar COLLATE "ucs_basic",
  ALTER COLUMN publisher TYPE varchar COLLATE "ucs_basic",
  ALTER COLUMN "version" TYPE varchar COLLATE "ucs_basic";

ALTER TABLE persons ALTER COLUMN gender TYPE varchar COLLATE "ucs_basic";

ALTER TABLE websites ALTER COLUMN url TYPE varchar COLLATE "ucs_basic";

ALTER TABLE fileresources ALTER COLUMN filename TYPE varchar COLLATE "ucs_basic",
  ALTER COLUMN mimetype TYPE varchar COLLATE "ucs_basic",
  ALTER COLUMN uri TYPE varchar COLLATE "ucs_basic",
  ALTER COLUMN http_base_url TYPE varchar COLLATE "ucs_basic";

ALTER TABLE fileresources_linkeddata ALTER COLUMN context TYPE varchar(512) COLLATE "ucs_basic",
  ALTER COLUMN object_type TYPE varchar(512) COLLATE "ucs_basic";

ALTER TABLE givennames ALTER COLUMN gender TYPE varchar COLLATE "ucs_basic";

ALTER TABLE headwords ALTER COLUMN "label" TYPE varchar(256) COLLATE "ucs_basic",
  ALTER COLUMN locale TYPE varchar COLLATE "ucs_basic";

ALTER TABLE humansettlements ALTER COLUMN settlement_type TYPE varchar COLLATE "ucs_basic";

ALTER TABLE identifiers ALTER COLUMN "namespace" TYPE varchar COLLATE "ucs_basic",
  ALTER COLUMN identifier TYPE varchar COLLATE "ucs_basic";

ALTER TABLE identifiertypes ALTER COLUMN "label" TYPE varchar COLLATE "ucs_basic",
  ALTER COLUMN "namespace" TYPE varchar COLLATE "ucs_basic",
  ALTER COLUMN pattern TYPE varchar COLLATE "ucs_basic";

ALTER TABLE licenses ALTER COLUMN acronym TYPE varchar COLLATE "ucs_basic",
  ALTER COLUMN url TYPE varchar COLLATE "ucs_basic";

ALTER TABLE predicates ALTER COLUMN value TYPE varchar COLLATE "ucs_basic";

ALTER TABLE rendering_templates ALTER COLUMN "name" TYPE varchar COLLATE "ucs_basic";

ALTER TABLE url_aliases ALTER COLUMN slug TYPE varchar(256) COLLATE "ucs_basic",
  ALTER COLUMN target_identifiable_type TYPE varchar COLLATE "ucs_basic",
  ALTER COLUMN target_entity_type TYPE varchar COLLATE "ucs_basic",
  ALTER COLUMN target_language TYPE varchar(2) COLLATE "ucs_basic";

ALTER TABLE users ALTER COLUMN email TYPE varchar COLLATE "ucs_basic",
  ALTER COLUMN firstname TYPE varchar COLLATE "ucs_basic",
  ALTER COLUMN lastname TYPE varchar COLLATE "ucs_basic";

ALTER TABLE versions ALTER COLUMN description TYPE varchar COLLATE "ucs_basic",
  ALTER COLUMN type_key TYPE varchar COLLATE "ucs_basic",
  ALTER COLUMN instance_key TYPE varchar COLLATE "ucs_basic",
  ALTER COLUMN instance_version_key TYPE varchar COLLATE "ucs_basic",
  ALTER COLUMN status TYPE varchar(16) COLLATE "ucs_basic";
