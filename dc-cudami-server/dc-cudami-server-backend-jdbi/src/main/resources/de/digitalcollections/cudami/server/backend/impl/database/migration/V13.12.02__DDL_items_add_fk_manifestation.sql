ALTER TABLE items
  ADD CONSTRAINT fk_items_manifestation FOREIGN KEY (manifestation) REFERENCES manifestations;

