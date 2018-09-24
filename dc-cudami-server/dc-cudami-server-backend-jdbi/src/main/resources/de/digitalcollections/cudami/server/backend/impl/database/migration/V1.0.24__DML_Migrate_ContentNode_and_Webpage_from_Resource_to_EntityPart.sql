UPDATE identifiables 
  SET identifiable_type='ENTITY_PART'
  FROM identifiables AS i INNER JOIN resources AS r ON i.uuid=r.uuid 
  WHERE r.resource_type='CONTENT_NODE' OR r.resource_type='WEBPAGE'
;

DELETE
  FROM resources
  WHERE resource_type='CONTENT_NODE' OR resource_type='WEBPAGE'
;