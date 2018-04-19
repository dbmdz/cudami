package de.digitalcollections.cudami.client.feign.exceptions;

import feign.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class HttpException extends Exception {

  int statuscode;
  String errorMessage;

  public HttpException(String methodKey, Response response) {
    super(String.format("Got %d for backend call %s.%n⤷ %s",
            response.status(), methodKey, response.request()));

    if (response.body() != null) {

      try {
        try (InputStreamReader ir = new InputStreamReader(response.body().asInputStream())) {
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
