package de.digitalcollections.model.jackson.mixin.identifiable.entity.manifestation;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.entity.manifestation.Publisher;

@JsonDeserialize(as = Publisher.class)
@JsonTypeInfo(use = Id.NAME, property = "objectType")
@JsonTypeName("PUBLISHER")
public interface PublisherMixIn {}
