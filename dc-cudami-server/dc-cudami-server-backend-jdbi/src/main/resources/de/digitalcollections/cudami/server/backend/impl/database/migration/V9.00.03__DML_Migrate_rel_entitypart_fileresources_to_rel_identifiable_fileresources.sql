INSERT INTO rel_identifiable_fileresources(identifiable_uuid, fileresource_uuid, sortIndex)
SELECT p.entitypart_uuid as identifiable_uuid, p.fileresource_uuid as fileresource_uuid, p.sortIndex as sortIndex
FROM rel_entitypart_fileresources p;
