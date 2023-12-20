package de.digitalcollections.model.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class DigitalCollectionsObjectMapper extends ObjectMapper {

  public DigitalCollectionsObjectMapper() {
    super();
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    registerModule(new Jdk8Module());
    registerModule(new JavaTimeModule());
    registerModule(new DigitalCollectionsModelModule());
  }

  private DigitalCollectionsObjectMapper(DigitalCollectionsObjectMapper objectMapper) {
    super(objectMapper);
  }

  @Override
  public ObjectMapper copy() {
    return new DigitalCollectionsObjectMapper(this);
  }
}
