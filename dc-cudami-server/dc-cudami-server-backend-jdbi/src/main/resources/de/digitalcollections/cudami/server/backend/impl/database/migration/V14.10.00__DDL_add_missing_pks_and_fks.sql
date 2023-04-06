ALTER TABLE familynames
	ADD CONSTRAINT familynames_pkey PRIMARY KEY (uuid),
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE geolocations
	ADD CONSTRAINT geolocations_pkey PRIMARY KEY (uuid),
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE givennames
	ADD CONSTRAINT givennames_pkey PRIMARY KEY (uuid),
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;


ALTER TABLE identifiables
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE entities
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE agents
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE articles
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE collections
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE corporatebodies
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE fileresources
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE fileresources_application
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE fileresources_audio
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE fileresources_image
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE fileresources_linkeddata
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE fileresources_text
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE fileresources_video
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE headwordentries
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image,
	ADD CONSTRAINT fk_headword FOREIGN KEY (headword) REFERENCES headwords;

ALTER TABLE persons
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image,
	ADD CONSTRAINT fk_locationofbirth FOREIGN KEY (locationofbirth) REFERENCES geolocations,
	ADD CONSTRAINT fk_locationofdeath FOREIGN KEY (locationofdeath) REFERENCES geolocations;

ALTER TABLE projects
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE topics
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE webpages
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE websites
	ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

