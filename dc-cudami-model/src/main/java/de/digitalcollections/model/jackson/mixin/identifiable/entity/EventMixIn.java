package de.digitalcollections.model.jackson.mixin.identifiable.entity;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.entity.Event;

@JsonDeserialize(as = Event.class)
@JsonTypeName("EVENT")
public interface EventMixIn extends EntityMixIn {}
