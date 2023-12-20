package de.digitalcollections.model.jackson.mixin.identifiable.entity.relation;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;

@JsonDeserialize(as = EntityRelation.class)
@JsonTypeName("ENTITY_RELATION")
public interface EntityRelationMixIn {}
