ALTER TABLE collections DROP COLUMN IF EXISTS id;
ALTER TABLE collections DROP CONSTRAINT IF EXISTS collections_pkey;
ALTER TABLE collections ADD PRIMARY KEY (uuid);

ALTER TABLE corporatebodies DROP COLUMN IF EXISTS id;
ALTER TABLE corporatebodies DROP CONSTRAINT IF EXISTS corporatebodies_pkey;
ALTER TABLE corporatebodies ADD PRIMARY KEY (uuid);

ALTER TABLE projects DROP COLUMN IF EXISTS id; 
ALTER TABLE projects DROP CONSTRAINT IF EXISTS projects_pkey;
ALTER TABLE projects ADD PRIMARY KEY (uuid);

