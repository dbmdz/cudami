package de.digitalcollections.model.jackson.mixin.identifiable.relation;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.relation.IdentifiableToFileResourceRelation;

@JsonDeserialize(as = IdentifiableToFileResourceRelation.class)
@JsonTypeName("IDENTIFIABLE_TO_FILERESOURCE_RELATION")
public interface IdentifiableToFileResourceRelationMixIn {}
