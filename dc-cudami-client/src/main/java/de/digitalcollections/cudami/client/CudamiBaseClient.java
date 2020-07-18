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
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;

public class CudamiBaseClient<T extends Object> {

  protected final HttpClient http;
  protected final ObjectMapper mapper;
  protected final ObjectReader reader;
  protected final URI serverUri;
  protected final Class<T> targetType;

  public CudamiBaseClient(String serverUrl, Class<T> targetType) {
    http =
        HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    this.mapper = new DigitalCollectionsObjectMapper();
    this.reader = mapper.reader().forType(targetType);
    this.serverUri = URI.create(serverUrl);
    this.targetType = targetType;
  }

  private HttpRequest createGetRequest(String requestUrl) {
    HttpRequest req =
        HttpRequest.newBuilder()
            .GET()
            .uri(serverUri.resolve(requestUrl))
            .header("Accept", "application/json")
            // TODO add creation of a request id if needed
            //            .header("X-Request-Id", request.getRequestId())
            .build();
    return req;
  }

  private HttpRequest createPostRequest(String requestUrl, Object bodyObject)
      throws JsonProcessingException {
    HttpRequest req =
        HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(bodyObject)))
            .uri(serverUri.resolve(requestUrl))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            // TODO add creation of a request id if needed
            //            .header("X-Request-Id", request.getRequestId())
            .build();
    return req;
  }

  private HttpRequest createPutRequest(String requestUrl, T bodyObject)
      throws JsonProcessingException {
    HttpRequest req =
        HttpRequest.newBuilder()
            .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(bodyObject)))
            .uri(serverUri.resolve(requestUrl))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            // TODO add creation of a request id if needed
            //            .header("X-Request-Id", request.getRequestId())
            .build();
    return req;
  }

  protected T doGetRequestForObject(String requestUrl) throws HttpException {
    return (T) doGetRequestForObject(requestUrl, targetType);
  }

  protected Object doGetRequestForObject(String requestUrl, Class<?> targetType)
      throws HttpException {
    HttpRequest req = createGetRequest(requestUrl);
    try {
      // This is the most performant approach for Jackson
      HttpResponse<byte[]> resp = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
      if (resp.statusCode() != 200) {
        throw CudamiRestErrorDecoder.decode("doGetRequestForObject", resp.statusCode());
      }
      T result = mapper.readerFor(targetType).readValue(resp.body());
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
      // This is the most performant approach for Jackson
      HttpResponse<byte[]> resp = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
      if (resp.statusCode() != 200) {
        throw CudamiRestErrorDecoder.decode("doGetRequestForObjectList", resp.statusCode());
      }
      List result = mapper.readerForListOf(targetType).readValue(resp.body());
      return result;
    } catch (IOException | InterruptedException e) {
      throw new HttpException("Failed to retrieve response due to connection error", e);
    }
  }

  protected PageResponse<T> doGetRequestForPagedObjectList(
      String requestUrl, PageRequest pageRequest) throws HttpException {
    FindParams findParams = getFindParams(pageRequest);
    requestUrl =
        requestUrl
            + "?"
            + String.format(
                "pageNumber=%d&pageSize=%d&sortField=%s&sortDirection=%s&nullHandling=%s",
                findParams.getPageNumber(),
                findParams.getPageSize(),
                findParams.getSortField(),
                findParams.getSortDirection(),
                findParams.getNullHandling());
    HttpRequest req = createGetRequest(requestUrl);
    try {
      // This is the most performant approach for Jackson
      HttpResponse<byte[]> resp = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
      if (resp.statusCode() != 200) {
        throw CudamiRestErrorDecoder.decode("doGetRequestForPagedObjectList", resp.statusCode());
      }
      PageResponse<T> result = mapper.readerFor(PageResponse.class).readValue(resp.body());
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
      // This is the most performant approach for Jackson
      HttpResponse<byte[]> resp = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
      if (resp.statusCode() != 200) {
        throw CudamiRestErrorDecoder.decode("doGetRequestForPagedObjectList", resp.statusCode());
      }
      SearchPageResponse<T> result =
          mapper.readerFor(SearchPageResponse.class).readValue(resp.body());
      result.setQuery(searchTerm);
      return result;
    } catch (IOException | InterruptedException e) {
      throw new HttpException("Failed to retrieve response due to connection error", e);
    }
  }

  protected String doGetRequestForString(String requestUrl) throws HttpException {
    HttpRequest req = createGetRequest(requestUrl);
    try {
      HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
      if (resp.statusCode() != 200) {
        throw CudamiRestErrorDecoder.decode("doGetRequestForString", resp.statusCode());
      }
      return resp.body();
    } catch (IOException | InterruptedException e) {
      throw new HttpException("Failed to retrieve response due to connection error", e);
    }
  }

  protected T doPostRequestForObject(String requestUrl, T object) throws HttpException {
    try {
      HttpRequest req = createPostRequest(requestUrl, object);
      // This is the most performant approach for Jackson
      HttpResponse<byte[]> resp = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
      if (resp.statusCode() != 200) {
        throw CudamiRestErrorDecoder.decode("doPostRequestForObject", resp.statusCode());
      }
      T result = mapper.readerFor(targetType).readValue(resp.body());
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
      // This is the most performant approach for Jackson
      HttpResponse<byte[]> resp = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
      if (resp.statusCode() != 200) {
        throw CudamiRestErrorDecoder.decode("doPostRequestForObject", resp.statusCode());
      }
      List<Class<?>> result = mapper.readerForListOf(targetType).readValue(resp.body());
      return result;
    } catch (IOException | InterruptedException e) {
      throw new HttpException("Failed to retrieve response due to error", e);
    }
  }

  protected T doPutRequestForObject(String requestUrl, T object) throws HttpException {
    try {
      HttpRequest req = createPutRequest(requestUrl, object);
      // This is the most performant approach for Jackson
      HttpResponse<byte[]> resp = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
      if (resp.statusCode() != 200) {
        throw CudamiRestErrorDecoder.decode("doPutRequestForObject", resp.statusCode());
      }
      T result = mapper.readerFor(targetType).readValue(resp.body());
      return result;
    } catch (IOException | InterruptedException e) {
      throw new HttpException("Failed to retrieve response due to connection error", e);
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

    Sorting sorting = pageRequest.getSorting();
    Iterator<Order> iterator = sorting.iterator();

    String sortField = "";
    String sortDirection = "";
    String nullHandling = "";

    if (iterator.hasNext()) {
      Order order = iterator.next();
      sortField = order.getProperty() == null ? "" : order.getProperty();
      sortDirection = order.getDirection() == null ? "" : order.getDirection().name();
      nullHandling = order.getNullHandling() == null ? "" : order.getNullHandling().name();
    }

    return new FindParamsImpl(pageNumber, pageSize, sortField, sortDirection, nullHandling);
  }
}
