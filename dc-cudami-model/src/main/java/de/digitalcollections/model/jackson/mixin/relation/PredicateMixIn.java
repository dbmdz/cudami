package de.digitalcollections.model.jackson.mixin.relation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.jackson.mixin.UniqueObjectMixIn;
import de.digitalcollections.model.relation.Predicate;

@JsonDeserialize(as = Predicate.class)
public interface PredicateMixIn extends UniqueObjectMixIn {}
