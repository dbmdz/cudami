ALTER TABLE items ADD CONSTRAINT fk_part_of_items_items_uuid FOREIGN KEY (part_of_item) REFERENCES items (uuid);

CREATE TRIGGER tr_items_previewfileresource
BEFORE INSERT OR UPDATE
ON items
FOR EACH ROW
EXECUTE FUNCTION check_uuid_exists('fileresources', 'previewfileresource');
