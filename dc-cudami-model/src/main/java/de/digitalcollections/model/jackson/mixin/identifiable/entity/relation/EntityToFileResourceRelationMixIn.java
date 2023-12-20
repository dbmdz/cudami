package de.digitalcollections.model.jackson.mixin.identifiable.entity.relation;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.entity.relation.EntityToFileResourceRelation;

@JsonDeserialize(as = EntityToFileResourceRelation.class)
@JsonTypeName("ENTITY_TO_FILERESOURCE_RELATION")
public interface EntityToFileResourceRelationMixIn {}
