package de.digitalcollections.model.jackson.mixin.identifiable.resource;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.resource.TextFileResource;

@JsonDeserialize(as = TextFileResource.class)
public interface TextFileResourceMixIn extends FileResourceMixIn {}
