package de.digitalcollections.model.jackson.mixin.identifiable.resource;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.resource.ApplicationFileResource;

@JsonDeserialize(as = ApplicationFileResource.class)
public interface ApplicationFileResourceMixIn extends FileResourceMixIn {}
