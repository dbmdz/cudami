/*
There is something special about INHERITANCE:
https://stackoverflow.com/questions/24360312/foreign-keys-table-inheritance-in-postgresql

Foreign keys that reference `identifiables` do not work for us.
`identifiables` is the parent of lots of other tables. A

    `select * from identifiables;`

collects the data of all children of `identifiables` (the columns existing in `identifiables` only),
that's why we see a huge amount of entries.
On the contrary a foreign key constraint checks only for presence of the value
in the referenced table ITSELF but not its child tables.
You can check the content of `identifiables` itself with 

    `select * from ONLY identifiables;`

and will see that there are no entries. `identifiables` itself is actually empty.
That's of course the reason why we always get a foreign key violation.
*/

ALTER TABLE rel_identifiable_entities DROP CONSTRAINT IF EXISTS rel_identifiable_entities_identifiable_uuid_fkey;
ALTER TABLE rel_identifiable_fileresources DROP CONSTRAINT IF EXISTS rel_identifiable_fileresources_identifiable_uuid_fkey;
