package de.digitalcollections.cudami.lobid.client.model.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class LobidObjectMapper extends ObjectMapper {

  public LobidObjectMapper() {
    super();
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    setSerializationInclusion(JsonInclude.Include.NON_NULL);
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    registerModule(new LobidModelModule());
  }
}
