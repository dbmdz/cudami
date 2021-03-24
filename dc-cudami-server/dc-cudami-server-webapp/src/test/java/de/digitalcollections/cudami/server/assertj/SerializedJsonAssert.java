package de.digitalcollections.cudami.server.assertj;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.AbstractAssert;

public class SerializedJsonAssert extends AbstractAssert<SerializedJsonAssert, String> {

  private ObjectMapper objectMapper;
  private Object actual;
  private String actualSerializedJson;

  protected SerializedJsonAssert(String actualSerializedJson) throws JsonProcessingException {
    super(actualSerializedJson, SerializedJsonAssert.class);
    this.actualSerializedJson = actualSerializedJson;
    objectMapper = new ObjectMapper();
    this.actual = objectMapper.readValue(actualSerializedJson, Object.class);
  }


  public void isSemanticallyEqualTo(String expectedSerializedJson) throws JsonProcessingException {
    Object expected = objectMapper.readValue(expectedSerializedJson, Object.class);

    if (actual==null && expected !=null) {
      failWithMessage("Expected non null json data");
    }

    if (actual!=null && expected ==null) {
      failWithMessage("Expected null json data");
    }

    if (!actual.equals(expected)) {
      failWithMessage("actual and expected differ, see formatted difference:\nExpected=%s\nActual=%s",
          objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(expected),
          objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(actual));
    }
  }
}
