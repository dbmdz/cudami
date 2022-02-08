package de.digitalcollections.cudami.lobid.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.exception.http.HttpErrorDecoder;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LobidBaseClient<T extends Object> {

  private static final Logger LOGGER = LoggerFactory.getLogger(LobidBaseClient.class);

  protected final HttpClient http;
  protected final ObjectMapper mapper;
  protected final ObjectReader reader;
  protected final URI serverUri;
  protected final Class<T> targetType;

  public LobidBaseClient(
      HttpClient http, String serverUrl, Class<T> targetType, ObjectMapper mapper) {
    this.http = http;
    this.mapper = mapper;
    this.reader = mapper.reader().forType(targetType);
    this.serverUri = URI.create(serverUrl);
    this.targetType = targetType;
  }

  protected URI createFullUri(String requestUrl) {
    return serverUri.resolve(serverUri.getPath() + requestUrl);
  }

  private HttpRequest createGetRequest(String requestUrl) {
    final URI url = createFullUri(requestUrl);
    LOGGER.debug("GET " + url);
    HttpRequest req =
        HttpRequest.newBuilder()
            .GET()
            .uri(url)
            .header("Accept", "application/json")
            // TODO add creation of a request id if needed
            //            .header("X-Request-Id", request.getRequestId())
            .build();
    return req;
  }

  protected T doGetRequestForObject(String requestUrl) throws TechnicalException {
    return (T) doGetRequestForObject(requestUrl, targetType);
  }

  protected Object doGetRequestForObject(String requestUrl, Class<?> targetType)
      throws TechnicalException {
    HttpRequest req = createGetRequest(requestUrl);
    try {
      HttpResponse<byte[]> response = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
      Integer statusCode = response.statusCode();
      if (statusCode >= 400) {
        throw HttpErrorDecoder.decode("GET " + requestUrl, statusCode, response);
      }
      // This is the most performant approach for Jackson
      final byte[] body = response.body();
      if (body == null || body.length == 0) {
        return null;
      }
      T result = mapper.readerFor(targetType).readValue(body);
      return result;
    } catch (IOException | InterruptedException e) {
      LOGGER.warn("Failed to retrieve response due to connection error", e);
      throw HttpErrorDecoder.decode("GET " + requestUrl, 500, null);
    }
  }

  protected List doGetRequestForObjectList(String requestUrl, Class<?> targetType)
      throws TechnicalException {
    HttpRequest req = createGetRequest(requestUrl);
    // TODO add creation of a request id if needed
    //            .header("X-Request-Id", request.getRequestId())
    try {
      HttpResponse<byte[]> response = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
      Integer statusCode = response.statusCode();
      if (statusCode >= 400) {
        throw HttpErrorDecoder.decode("GET " + requestUrl, statusCode, response);
      }
      // This is the most performant approach for Jackson
      final byte[] body = response.body();
      if (body == null || body.length == 0) {
        return null;
      }
      List result = mapper.readerForListOf(targetType).readValue(body);
      return result;
    } catch (IOException | InterruptedException e) {
      LOGGER.warn("Failed to retrieve response due to connection error", e);
      throw HttpErrorDecoder.decode("GET " + requestUrl, 500, null);
    }
  }

  protected String doGetRequestForString(String requestUrl) throws TechnicalException {
    HttpRequest req = createGetRequest(requestUrl);
    try {
      HttpResponse<String> response = http.send(req, HttpResponse.BodyHandlers.ofString());
      Integer statusCode = response.statusCode();
      if (statusCode >= 400) {
        throw HttpErrorDecoder.decode("GET " + requestUrl, statusCode, response);
      }
      final String body = response.body();
      return body;
    } catch (IOException | InterruptedException e) {
      LOGGER.warn("Failed to retrieve response due to connection error", e);
      throw HttpErrorDecoder.decode("GET " + requestUrl, 500, null);
    }
  }
}
