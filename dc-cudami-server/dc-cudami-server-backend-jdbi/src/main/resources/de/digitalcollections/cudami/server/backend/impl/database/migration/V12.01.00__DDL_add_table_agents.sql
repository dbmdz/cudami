CREATE TABLE IF NOT EXISTS agents (LIKE entities INCLUDING ALL, CONSTRAINT agents_pkey PRIMARY KEY (uuid)) INHERITS (entities);
ALTER TABLE corporatebodies NO INHERIT entities, INHERIT agents;
ALTER TABLE persons NO INHERIT entities, INHERIT agents;
