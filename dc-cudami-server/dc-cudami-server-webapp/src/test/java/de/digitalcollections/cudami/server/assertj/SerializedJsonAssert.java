package de.digitalcollections.cudami.server.assertj;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
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

    if (actual == null && expected != null) {
      failWithMessage("Expected non null json data");
    }

    if (actual != null && expected == null) {
      failWithMessage("Expected null json data");
    }

    if (!actual.equals(expected)) {
      failWithMessage(
          "actual and expected differ, see formatted difference:\nExpected=%s\nActual=%s",
          objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(expected),
          objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(actual));
    }
  }

  public void isSemanticallyEqualToJsonFromFile(String expectedJsonSource)
      throws URISyntaxException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    URL resource = classLoader.getResource("json/" + expectedJsonSource);
    isSemanticallyEqualTo(Files.readString(Path.of(resource.toURI())));
  }
}
