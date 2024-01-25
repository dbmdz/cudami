package de.digitalcollections.model.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import java.io.IOException;
import java.nio.charset.Charset;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseJsonSerializationTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(BaseJsonSerializationTest.class);

  private static final ObjectMapper OBJECT_MAPPER = new DigitalCollectionsObjectMapper();

  protected <T> void checkSerializeDeserialize(T objectIn) throws Exception {
    checkSerializeDeserialize(objectIn, null);
  }

  protected <T> T checkSerializeDeserialize(T objectIn, String pathToJson) throws Exception {
    T objectOut = serializeDeserialize(objectIn, pathToJson);

    // FIXME: What is it good for? It makes trouble and works better w/o though.
    //    try {
    //      Set<String> keys = BeanUtils.describe(objectIn).keySet();
    //      for (String key : keys) {
    //        if ("UUID".equals(key)) {
    //          BeanUtils.setProperty(objectIn, key, null);
    //          BeanUtils.setProperty(objectOut, key, null);
    //        } else {
    //          BeanUtils.setProperty(objectIn, key + ".UUID", null);
    //          BeanUtils.setProperty(objectOut, key + ".UUID", null);
    //        }
    //      }
    //    } catch (InvocationTargetException e) {
    //      LOGGER.warn(e.toString());
    //    }
    try {
      assertThat(objectOut).usingRecursiveComparison().isEqualTo(objectIn);
    } catch (Throwable e) {
      LOGGER.error(
          "ERR: IN="
              + dump(objectIn)
              + "\nOUT="
              + dump(objectOut)
              + "\n\nERROR="
              + e.getClass()
              + "="
              + e.getMessage());
      throw e;
    }
    return objectOut;
  }

  private String dump(Object o) throws JsonProcessingException {
    return getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  protected ObjectMapper getMapper() {
    return OBJECT_MAPPER;
  }

  protected String readFromResources(String filename) throws IOException {
    return Resources.toString(Resources.getResource(filename), Charset.defaultCharset());
  }

  private <T> T serializeDeserialize(T o, String pathToJson)
      throws JsonProcessingException, IOException {
    String serializedObject = dump(o);
    if (pathToJson != null) {
      Assertions.assertEquals(readFromResources(pathToJson), serializedObject);
    }

    Class valueType = o.getClass();
    T deserializedObject = (T) getMapper().readValue(serializedObject, valueType);
    return deserializedObject;
  }
}
