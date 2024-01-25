package de.digitalcollections.model.jackson.mixin.identifiable.resource;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.resource.AudioFileResource;

@JsonDeserialize(as = AudioFileResource.class)
public interface AudioFileResourceMixIn extends FileResourceMixIn {}
