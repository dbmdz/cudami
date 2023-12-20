package de.digitalcollections.model.jackson.mixin.time;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.time.TimeValue;

@JsonDeserialize(as = TimeValue.class)
@JsonTypeName("TIMEVALUE")
public interface TimeValueMixIn {}
