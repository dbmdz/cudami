package de.digitalcollections.cudami.server.assertj;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.assertj.core.api.Assert;
import org.springframework.http.HttpStatus;

public class Assertions {

  public static SerializedJsonAssert assertThat(String serializedJson) throws JsonProcessingException {
    return new SerializedJsonAssert(serializedJson);
  }

  public static Assert assertThat(HttpStatus httpStatus) {
    return org.assertj.core.api.Assertions.assertThat(httpStatus);
  }

}
