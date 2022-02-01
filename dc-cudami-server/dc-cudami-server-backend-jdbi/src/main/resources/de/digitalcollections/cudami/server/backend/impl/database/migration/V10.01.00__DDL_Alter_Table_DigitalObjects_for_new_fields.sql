-- add a reference to the item and set the uuid as primary key for items
ALTER TABLE items ADD PRIMARY KEY(uuid);
ALTER TABLE digitalobjects ADD COLUMN item_uuid UUID;
ALTER TABLE digitalobjects ADD CONSTRAINT fk_item FOREIGN KEY(item_uuid) REFERENCES items(uuid);

-- add a reference to the parent digital object
ALTER TABLE digitalobjects ADD COLUMN parent_uuid UUID;
ALTER TABLE digitalobjects ADD CONSTRAINT fk_parent FOREIGN KEY(parent_uuid) REFERENCES digitalobjects(uuid);

-- add the number of binary resources
ALTER TABLE digitalobjects ADD column number_binaryresources INTEGER;

-- add the reference to the license
ALTER TABLE digitalobjects ADD COLUMN license_uuid UUID;
ALTER TABLE digitalobjects ADD CONSTRAINT fk_license FOREIGN KEY(license_uuid) REFERENCES licenses(uuid);

-- add the reference to the place of the creation
-- since the place types use distinct tables, e.g. humansettlements,
-- we cannot use a foreign key here
ALTER TABLE digitalobjects ADD COLUMN creation_geolocation_uuid UUID;

-- add the date of the creation
ALTER TABLE digitalobjects ADD COLUMN creation_date DATE;

-- add the reference to the agent of the creation
-- since the agent types use distinct tables, e.g. person,
-- we cannot use a foreign key here
ALTER TABLE digitalobjects ADD COLUMN creation_creator_uuid UUID;
