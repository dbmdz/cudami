package de.digitalcollections.cudami.server.assertj;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.assertj.core.api.Assert;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public class Assertions {

  public static SerializedJsonAssert assertThat(String serializedJson) throws JsonProcessingException {
    return new SerializedJsonAssert(serializedJson);
  }

  public static Assert assertThat(HttpStatus httpStatus) {
    return org.assertj.core.api.Assertions.assertThat(httpStatus);
  }

  public static Assert assertThat(MediaType mediaType) {
    return org.assertj.core.api.Assertions.assertThat(mediaType);
  }

  public static HttpHeadersAssert assertThat(HttpHeaders headers) {
    return new HttpHeadersAssert(headers);
  }

}
