ALTER TABLE familynames
	DROP CONSTRAINT IF EXISTS familynames_pkey,
	ADD CONSTRAINT familynames_pkey PRIMARY KEY (uuid),
	DROP CONSTRAINT IF EXISTS fk_previewfileresource,
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE givennames
	DROP CONSTRAINT IF EXISTS givennames_pkey,
	ADD CONSTRAINT givennames_pkey PRIMARY KEY (uuid),
	DROP CONSTRAINT IF EXISTS fk_previewfileresource,
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;


ALTER TABLE identifiables
	DROP CONSTRAINT IF EXISTS fk_previewfileresource,
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE entities
	DROP CONSTRAINT IF EXISTS fk_previewfileresource,
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE agents
	DROP CONSTRAINT IF EXISTS fk_previewfileresource,
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE articles
	DROP CONSTRAINT IF EXISTS fk_previewfileresource,
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE collections
	DROP CONSTRAINT IF EXISTS fk_previewfileresource,
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE corporatebodies
	DROP CONSTRAINT IF EXISTS fk_previewfileresource,
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE fileresources
	DROP CONSTRAINT IF EXISTS fk_previewfileresource,
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE fileresources_application
	DROP CONSTRAINT IF EXISTS fk_previewfileresource,
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE fileresources_audio
	DROP CONSTRAINT IF EXISTS fk_previewfileresource,
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE fileresources_image
	DROP CONSTRAINT IF EXISTS fk_previewfileresource,
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE fileresources_linkeddata
	DROP CONSTRAINT IF EXISTS fk_previewfileresource,
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE fileresources_text
	DROP CONSTRAINT IF EXISTS fk_previewfileresource,
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE fileresources_video
	DROP CONSTRAINT IF EXISTS fk_previewfileresource,
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE headwordentries
	DROP CONSTRAINT IF EXISTS fk_previewfileresource,
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image,
	DROP CONSTRAINT IF EXISTS fk_headword,
	ADD CONSTRAINT fk_headword FOREIGN KEY (headword) REFERENCES headwords;

ALTER TABLE persons
	DROP CONSTRAINT IF EXISTS fk_locationofbirth,
	DROP CONSTRAINT IF EXISTS fk_locationofdeath,
	DROP CONSTRAINT IF EXISTS fk_previewfileresource,
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE geolocations
	DROP CONSTRAINT IF EXISTS geolocations_pkey,
	ADD CONSTRAINT geolocations_pkey PRIMARY KEY (uuid),
	DROP CONSTRAINT IF EXISTS fk_previewfileresource,
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

CREATE TRIGGER tr_persons_geolocations_locofbirth
BEFORE INSERT OR UPDATE
ON persons
FOR EACH ROW
EXECUTE FUNCTION check_uuid_exists('geolocations', 'locationofbirth');

CREATE TRIGGER tr_persons_geolocations_locofdeath
BEFORE INSERT OR UPDATE
ON persons
FOR EACH ROW
EXECUTE FUNCTION check_uuid_exists('geolocations', 'locationofdeath');

CREATE TRIGGER tr_geolocations_persons_prevent_removal
AFTER DELETE
ON geolocations
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('persons', 'locationofbirth', 'persons', 'locationofdeath');

CREATE TRIGGER tr_humansettlements_persons_prevent_removal
AFTER DELETE
ON humansettlements
FOR EACH ROW
EXECUTE FUNCTION is_not_referenced_by('persons', 'locationofbirth', 'persons', 'locationofdeath');

ALTER TABLE projects
	DROP CONSTRAINT IF EXISTS fk_previewfileresource,
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE topics
	DROP CONSTRAINT IF EXISTS fk_previewfileresource,
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE webpages
	DROP CONSTRAINT IF EXISTS fk_previewfileresource,
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE websites
	DROP CONSTRAINT IF EXISTS fk_previewfileresource,
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

