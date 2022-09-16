ALTER TABLE entities ADD CONSTRAINT pk_entities PRIMARY KEY (uuid);

ALTER TABLE humansettlements
  ADD CONSTRAINT pk_humansettlements PRIMARY KEY (uuid),
  ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

ALTER TABLE works ADD CONSTRAINT pk_works PRIMARY KEY (uuid);

