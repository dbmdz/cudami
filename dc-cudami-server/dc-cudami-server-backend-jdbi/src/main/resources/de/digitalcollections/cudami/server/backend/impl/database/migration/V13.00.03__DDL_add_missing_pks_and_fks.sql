ALTER TABLE humansettlements
  ADD CONSTRAINT pk_humansettlements PRIMARY KEY (uuid),
  ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

