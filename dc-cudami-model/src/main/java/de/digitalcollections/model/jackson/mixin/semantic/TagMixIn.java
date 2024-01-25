package de.digitalcollections.model.jackson.mixin.semantic;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.jackson.mixin.UniqueObjectMixIn;
import de.digitalcollections.model.semantic.Tag;

@JsonDeserialize(as = Tag.class)
@JsonTypeName("TAG")
public interface TagMixIn extends UniqueObjectMixIn {}
