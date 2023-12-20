package de.digitalcollections.model.jackson.mixin.identifiable.entity.manifestation;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.entity.manifestation.ExpressionType;
import de.digitalcollections.model.jackson.mixin.MainSubTypeMixIn;

@JsonDeserialize(as = ExpressionType.class)
@JsonTypeName("EXPRESSION_TYPE")
public interface ExpressionTypeMixIn extends MainSubTypeMixIn {}
