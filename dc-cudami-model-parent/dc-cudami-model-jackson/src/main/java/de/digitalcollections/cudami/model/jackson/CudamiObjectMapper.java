package de.digitalcollections.cudami.model.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.digitalcollections.core.model.jackson.DcCoreModelModule;
import de.digitalcollections.prosemirror.model.jackson.ProseMirrorModule;

public class CudamiObjectMapper extends ObjectMapper {

  public CudamiObjectMapper() {
    super();
    customize(this);
  }

  public static ObjectMapper customize(ObjectMapper objectMapper) {
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.registerModule(new DcCoreModelModule());
    objectMapper.registerModule(new CudamiModule());
    objectMapper.registerModule(new ProseMirrorModule());
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return objectMapper;
  }
}
