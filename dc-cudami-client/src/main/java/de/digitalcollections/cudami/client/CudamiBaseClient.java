package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import de.digitalcollections.cudami.client.exceptions.CudamiRestErrorDecoder;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.paging.FindParams;
import de.digitalcollections.model.api.paging.Order;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.impl.paging.FindParamsImpl;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CudamiBaseClient<T extends Object> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CudamiBaseClient.class);

  protected final HttpClient http;
  protected final ObjectMapper mapper;
  protected final ObjectReader reader;
  protected final URI serverUri;
  protected final Class<T> targetType;

  public CudamiBaseClient(
      HttpClient http, String serverUrl, Class<T> targetType, ObjectMapper mapper) {
    this.http = http;
    this.mapper = mapper;
    this.reader = mapper.reader().forType(targetType);
    this.serverUri = URI.create(serverUrl);
    this.targetType = targetType;
  }

  private HttpRequest createDeleteRequest(String requestUrl) {
    final URI url = createFullUri(requestUrl);
    LOGGER.debug("DELETE " + url);
    HttpRequest req =
        HttpRequest.newBuilder()
            .DELETE()
            .uri(url)
            .header("Accept", "application/json")
            // TODO add creation of a request id if needed
            //            .header("X-Request-Id", request.getRequestId())
            .build();
    return req;
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

  private HttpRequest createPatchRequest(String requestUrl) {
    final URI url = createFullUri(requestUrl);
    LOGGER.debug("PATCH " + url);
    HttpRequest req =
        HttpRequest.newBuilder()
            .method("PATCH", null)
            .uri(url)
            .header("Accept", "application/json")
            // TODO add creation of a request id if needed
            //            .header("X-Request-Id", request.getRequestId())
            .build();
    return req;
  }

  private HttpRequest createPatchRequest(String requestUrl, Object bodyObject)
      throws JsonProcessingException {
    final URI url = createFullUri(requestUrl);
    LOGGER.debug("PATCH " + url + " with body");
    HttpRequest req =
        HttpRequest.newBuilder()
            .method(
                "PATCH", HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(bodyObject)))
            .uri(url)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            // TODO add creation of a request id if needed
            //            .header("X-Request-Id", request.getRequestId())
            .build();
    return req;
  }

  private HttpRequest createPostRequest(String requestUrl) throws JsonProcessingException {
    final URI url = createFullUri(requestUrl);
    LOGGER.debug("POST " + url);
    HttpRequest req =
        HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.noBody())
            .uri(url)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            // TODO add creation of a request id if needed
            //            .header("X-Request-Id", request.getRequestId())
            .build();
    return req;
  }

  private HttpRequest createPostRequest(String requestUrl, Object bodyObject)
      throws JsonProcessingException {
    final URI url = createFullUri(requestUrl);
    LOGGER.debug("POST " + url + " with body");
    HttpRequest req =
        HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(bodyObject)))
            .uri(url)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            // TODO add creation of a request id if needed
            //            .header("X-Request-Id", request.getRequestId())
            .build();
    return req;
  }

  private HttpRequest createPutRequest(String requestUrl, Object bodyObject)
      throws JsonProcessingException {
    final URI url = createFullUri(requestUrl);
    LOGGER.debug("PUT " + url + " with body");
    HttpRequest req =
        HttpRequest.newBuilder()
            .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(bodyObject)))
            .uri(url)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            // TODO add creation of a request id if needed
            //            .header("X-Request-Id", request.getRequestId())
            .build();
    return req;
  }

  protected String doDeleteRequestForString(String requestUrl) throws HttpException {
    HttpRequest req = createDeleteRequest(requestUrl);
    try {
      HttpResponse<String> response = http.send(req, HttpResponse.BodyHandlers.ofString());
      Integer statusCode = response.statusCode();
      if (statusCode != 200) {
        throw CudamiRestErrorDecoder.decode("DELETE " + requestUrl, statusCode);
      }
      final String body = response.body();
      return body;
    } catch (InterruptedException | IOException e) {
      throw new HttpException("Failed to retrieve response due to connection error", e);
    }
  }

  protected T doGetRequestForObject(String requestUrl) throws HttpException {
    return (T) doGetRequestForObject(requestUrl, targetType);
  }

  protected Object doGetRequestForObject(String requestUrl, Class<?> targetType)
      throws HttpException {
    HttpRequest req = createGetRequest(requestUrl);
    try {
      HttpResponse<byte[]> response = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
      Integer statusCode = response.statusCode();
      if (statusCode != 200) {
        throw CudamiRestErrorDecoder.decode("GET " + requestUrl, statusCode);
      }
      // This is the most performant approach for Jackson
      final byte[] body = response.body();
      if (body == null || body.length == 0) {
        return null;
      }
      T result = mapper.readerFor(targetType).readValue(body);
      return result;
    } catch (IOException | InterruptedException e) {
      throw new HttpException("Failed to retrieve response due to connection error", e);
    }
  }

  protected List<T> doGetRequestForObjectList(String requestUrl) throws HttpException {
    return (List<T>) doGetRequestForObjectList(requestUrl, targetType);
  }

  protected List doGetRequestForObjectList(String requestUrl, Class<?> targetType)
      throws HttpException {
    HttpRequest req = createGetRequest(requestUrl);
    // TODO add creation of a request id if needed
    //            .header("X-Request-Id", request.getRequestId())
    try {
      HttpResponse<byte[]> response = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
      Integer statusCode = response.statusCode();
      if (statusCode != 200) {
        throw CudamiRestErrorDecoder.decode("GET " + requestUrl, statusCode);
      }
      // This is the most performant approach for Jackson
      final byte[] body = response.body();
      if (body == null || body.length == 0) {
        return null;
      }
      List result = mapper.readerForListOf(targetType).readValue(body);
      return result;
    } catch (IOException | InterruptedException e) {
      throw new HttpException("Failed to retrieve response due to connection error", e);
    }
  }

  protected PageResponse<T> doGetRequestForPagedObjectList(
      String requestUrl, PageRequest pageRequest) throws HttpException {
    FindParams findParams = getFindParams(pageRequest);
    if (!requestUrl.contains("?")) {
      requestUrl = requestUrl + "?";
    } else {
      if (!requestUrl.endsWith("&")) {
        requestUrl = requestUrl + "&";
      }
    }
    requestUrl =
        requestUrl
            + String.format(
                "pageNumber=%d&pageSize=%d&sortField=%s&sortDirection=%s&nullHandling=%s",
                findParams.getPageNumber(),
                findParams.getPageSize(),
                findParams.getSortField(),
                findParams.getSortDirection(),
                findParams.getNullHandling());
    HttpRequest req = createGetRequest(requestUrl);
    try {
      HttpResponse<byte[]> response = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
      Integer statusCode = response.statusCode();
      if (statusCode != 200) {
        throw CudamiRestErrorDecoder.decode("GET " + requestUrl, statusCode);
      }
      // This is the most performant approach for Jackson
      final byte[] body = response.body();
      if (body == null || body.length == 0) {
        return null;
      }
      PageResponse<T> result = mapper.readerFor(PageResponse.class).readValue(body);
      return result;
    } catch (IOException | InterruptedException e) {
      throw new HttpException("Failed to retrieve response due to connection error", e);
    }
  }

  protected SearchPageResponse<T> doGetSearchRequestForPagedObjectList(
      String requestUrl, SearchPageRequest searchPageRequest) throws HttpException {
    FindParams findParams = getFindParams(searchPageRequest);
    String searchTerm = searchPageRequest.getQuery();
    requestUrl =
        requestUrl
            + "?"
            + String.format(
                "pageNumber=%d&pageSize=%d&sortField=%s&sortDirection=%s&nullHandling=%s&searchTerm=%s",
                findParams.getPageNumber(),
                findParams.getPageSize(),
                findParams.getSortField(),
                findParams.getSortDirection(),
                findParams.getNullHandling(),
                URLEncoder.encode(searchTerm, StandardCharsets.UTF_8));
    HttpRequest req = createGetRequest(requestUrl);
    try {
      HttpResponse<byte[]> response = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
      Integer statusCode = response.statusCode();
      if (statusCode != 200) {
        throw CudamiRestErrorDecoder.decode("GET " + requestUrl, statusCode);
      }
      // This is the most performant approach for Jackson
      final byte[] body = response.body();
      if (body == null || body.length == 0) {
        return null;
      }
      SearchPageResponse<T> result = mapper.readerFor(SearchPageResponse.class).readValue(body);
      result.setQuery(searchTerm);
      return result;
    } catch (IOException | InterruptedException e) {
      throw new HttpException("Failed to retrieve response due to connection error", e);
    }
  }

  protected PageResponse doGetRequestForPagedObjectList(
      String requestUrl, PageRequest pageRequest, Class<?> targetType) throws HttpException {
    FindParams findParams = getFindParams(pageRequest);
    if (!requestUrl.contains("?")) {
      requestUrl = requestUrl + "?";
    } else {
      if (!requestUrl.endsWith("&")) {
        requestUrl = requestUrl + "&";
      }
    }
    requestUrl =
        requestUrl
            + String.format(
                "pageNumber=%d&pageSize=%d&sortField=%s&sortDirection=%s&nullHandling=%s",
                findParams.getPageNumber(),
                findParams.getPageSize(),
                findParams.getSortField(),
                findParams.getSortDirection(),
                findParams.getNullHandling());
    HttpRequest req = createGetRequest(requestUrl);
    try {
      HttpResponse<byte[]> response = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
      Integer statusCode = response.statusCode();
      if (statusCode != 200) {
        throw CudamiRestErrorDecoder.decode("GET " + requestUrl, statusCode);
      }
      // This is the most performant approach for Jackson
      final byte[] body = response.body();
      if (body == null) {
        return null;
      }
      PageResponse result = mapper.readerFor(PageResponse.class).readValue(body);
      return result;
    } catch (IOException | InterruptedException e) {
      throw new HttpException("Failed to retrieve response due to connection error", e);
    }
  }

  protected String doGetRequestForString(String requestUrl) throws HttpException {
    HttpRequest req = createGetRequest(requestUrl);
    try {
      HttpResponse<String> response = http.send(req, HttpResponse.BodyHandlers.ofString());
      Integer statusCode = response.statusCode();
      if (statusCode != 200) {
        throw CudamiRestErrorDecoder.decode("GET " + requestUrl, statusCode);
      }
      final String body = response.body();
      return body;
    } catch (IOException | InterruptedException e) {
      throw new HttpException("Failed to retrieve response due to connection error", e);
    }
  }

  protected String doPatchRequestForString(String requestUrl) throws HttpException {
    HttpRequest req = createPatchRequest(requestUrl);
    try {
      HttpResponse<String> response = http.send(req, HttpResponse.BodyHandlers.ofString());
      Integer statusCode = response.statusCode();
      if (statusCode != 200) {
        throw CudamiRestErrorDecoder.decode("PATCH " + requestUrl, statusCode);
      }
      final String body = response.body();
      return body;
    } catch (InterruptedException | IOException e) {
      throw new HttpException("Failed to retrieve response due to connection error", e);
    }
  }

  protected String doPatchRequestForString(String requestUrl, Object object) throws HttpException {
    try {
      HttpRequest req = createPatchRequest(requestUrl, object);
      HttpResponse<String> response = http.send(req, HttpResponse.BodyHandlers.ofString());
      Integer statusCode = response.statusCode();
      if (statusCode != 200) {
        throw CudamiRestErrorDecoder.decode("PATCH " + requestUrl, statusCode);
      }
      final String body = response.body();
      return body;
    } catch (IOException | InterruptedException e) {
      throw new HttpException("Failed to retrieve response due to connection error", e);
    }
  }

  protected T doPostRequestForObject(String requestUrl, T object) throws HttpException {
    try {
      HttpRequest req = createPostRequest(requestUrl, object);
      HttpResponse<byte[]> response = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
      Integer statusCode = response.statusCode();
      if (statusCode != 200) {
        throw CudamiRestErrorDecoder.decode("POST " + requestUrl, statusCode);
      }
      // This is the most performant approach for Jackson
      final byte[] body = response.body();
      if (body == null || body.length == 0) {
        return null;
      }
      T result = mapper.readerFor(targetType).readValue(body);
      return result;
    } catch (IOException | InterruptedException e) {
      throw new HttpException("Failed to retrieve response due to error", e);
    }
  }

  protected Object doPostRequestForObject(String requestUrl, Object bodyObject, Class<?> targetType)
      throws HttpException {
    try {
      HttpRequest req = createPostRequest(requestUrl, bodyObject);
      HttpResponse<byte[]> response = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
      Integer statusCode = response.statusCode();
      if (statusCode != 200) {
        throw CudamiRestErrorDecoder.decode("POST " + requestUrl, statusCode);
      }
      // This is the most performant approach for Jackson
      final byte[] body = response.body();
      if (body == null) {
        return null;
      }
      Object result = mapper.readerFor(targetType).readValue(body);
      return result;
    } catch (IOException | InterruptedException e) {
      throw new HttpException("Failed to retrieve response due to error", e);
    }
  }

  protected Object doPostRequestForObject(String requestUrl, Class<?> targetType)
      throws HttpException {
    try {
      HttpRequest req = createPostRequest(requestUrl);
      HttpResponse<byte[]> response = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
      Integer statusCode = response.statusCode();
      if (statusCode != 200) {
        throw CudamiRestErrorDecoder.decode("POST " + requestUrl, statusCode);
      }
      // This is the most performant approach for Jackson
      final byte[] body = response.body();
      if (body == null || body.length == 0) {
        return null;
      }
      Object result = mapper.readerFor(targetType).readValue(body);
      return result;
    } catch (IOException | InterruptedException e) {
      throw new HttpException("Failed to retrieve response due to error", e);
    }
  }

  protected List<T> doPostRequestForObjectList(String requestUrl, List<T> list)
      throws HttpException {
    return (List<T>) doPostRequestForObjectList(requestUrl, (List<Class<?>>) list, targetType);
  }

  protected List<Class<?>> doPostRequestForObjectList(
      String requestUrl, List<Class<?>> list, Class<?> targetType) throws HttpException {
    try {
      HttpRequest req = createPostRequest(requestUrl, list);
      HttpResponse<byte[]> response = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
      Integer statusCode = response.statusCode();
      if (statusCode != 200) {
        throw CudamiRestErrorDecoder.decode("POST " + requestUrl, statusCode);
      }
      // This is the most performant approach for Jackson
      final byte[] body = response.body();
      if (body == null || body.length == 0) {
        return null;
      }
      List<Class<?>> result = mapper.readerForListOf(targetType).readValue(body);
      return result;
    } catch (IOException | InterruptedException e) {
      throw new HttpException("Failed to retrieve response due to error", e);
    }
  }

  protected T doPutRequestForObject(String requestUrl, T object) throws HttpException {
    try {
      HttpRequest req = createPutRequest(requestUrl, object);
      HttpResponse<byte[]> response = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
      Integer statusCode = response.statusCode();
      if (statusCode != 200) {
        throw CudamiRestErrorDecoder.decode("PUT " + requestUrl, statusCode);
      }
      // This is the most performant approach for Jackson
      final byte[] body = response.body();
      if (body == null || body.length == 0) {
        return null;
      }
      T result = mapper.readerFor(targetType).readValue(body);
      return result;
    } catch (IOException | InterruptedException e) {
      throw new HttpException("Failed to retrieve response due to connection error", e);
    }
  }

  protected Object doPutRequestForObject(String requestUrl, Object bodyObject, Class<?> targetType)
      throws HttpException {
    try {
      HttpRequest req = createPutRequest(requestUrl, bodyObject);
      HttpResponse<byte[]> response = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
      Integer statusCode = response.statusCode();
      if (statusCode != 200) {
        throw CudamiRestErrorDecoder.decode("PUT " + requestUrl, statusCode);
      }
      // This is the most performant approach for Jackson
      final byte[] body = response.body();
      if (body == null) {
        return null;
      }
      Object result = mapper.readerFor(targetType).readValue(body);
      return result;
    } catch (IOException | InterruptedException e) {
      throw new HttpException("Failed to retrieve response due to error", e);
    }
  }

  /**
   * Wrapper for find params
   *
   * @param pageRequest source for find params
   * @return wrapped find params
   */
  private FindParams getFindParams(PageRequest pageRequest) {
    int pageNumber = pageRequest.getPageNumber();
    int pageSize = pageRequest.getPageSize();

    String sortField = "";
    String sortDirection = "";
    String nullHandling = "";
    Sorting sorting = pageRequest.getSorting();
    if (sorting != null) {
      Iterator<Order> iterator = sorting.iterator();
      if (iterator.hasNext()) {
        Order order = iterator.next();
        sortField = order.getProperty() == null ? "" : order.getProperty();
        sortDirection = order.getDirection() == null ? "" : order.getDirection().name();
        nullHandling = order.getNullHandling() == null ? "" : order.getNullHandling().name();
      }
    }
    return new FindParamsImpl(pageNumber, pageSize, sortField, sortDirection, nullHandling);
  }
}
