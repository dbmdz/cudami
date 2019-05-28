ALTER TABLE identifiertypes DROP COLUMN id;
ALTER TABLE identifiertypes ADD COLUMN uuid UUID primary key;
