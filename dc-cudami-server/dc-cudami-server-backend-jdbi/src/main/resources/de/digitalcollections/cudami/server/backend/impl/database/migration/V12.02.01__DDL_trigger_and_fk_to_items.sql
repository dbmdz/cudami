ALTER TABLE items
  ADD CONSTRAINT fk_items_part_of_items_items_uuid FOREIGN KEY (part_of_item) REFERENCES items (uuid),
  ADD CONSTRAINT fk_items_previewfileresource_fileresources_image_uuid FOREIGN KEY (previewfileresource) REFERENCES fileresources_image (uuid);
