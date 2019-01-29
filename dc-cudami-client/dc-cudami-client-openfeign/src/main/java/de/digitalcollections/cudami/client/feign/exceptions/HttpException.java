package de.digitalcollections.cudami.client.feign.exceptions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import feign.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class HttpException extends Exception {

  int statuscode;
  String errorMessage;

  @SuppressFBWarnings(value = "OS_OPEN_STREAM", justification = "Opened stream will be closed via try-with-resources statement")
  public HttpException(String methodKey, Response response) {
    super(String.format("Got %d for backend call %s.%n⤷ %s",
        response.status(), methodKey, response.request()));

    if (response.body() != null) {

      try {
        try (InputStreamReader ir = new InputStreamReader(response.body().asInputStream(), StandardCharsets.UTF_8)) {
          errorMessage = new BufferedReader(ir).lines().parallel().collect(Collectors.joining(" "));
          errorMessage = errorMessage.replaceFirst("^\"", "").replaceFirst("\"$", "");
        }
      } catch (IOException e) {
        errorMessage = "Cannot read error message because of " + e.getMessage();
      }
    }

    this.statuscode = response.status();
  }

  public int getStatusCode() {
    return statuscode;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

}
