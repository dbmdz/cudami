package de.digitalcollections.model.jackson.mixin.identifiable.relation;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.relation.IdentifiableToEntityRelation;

@JsonDeserialize(as = IdentifiableToEntityRelation.class)
@JsonTypeName("IDENTIFIABLE_TO_ENTITY_RELATION")
public interface IdentifiableToEntityRelationMixIn {}
