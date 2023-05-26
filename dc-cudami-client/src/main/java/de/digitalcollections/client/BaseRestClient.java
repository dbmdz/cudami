package de.digitalcollections.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.exception.http.HttpErrorDecoder;
import de.digitalcollections.model.list.filtering.FilterCriteria;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterLogicalOperator;
import de.digitalcollections.model.list.filtering.FilterOperation;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.NullHandling;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressFBWarnings(value = "EI_EXPOSE_REP2")
public abstract class BaseRestClient<T extends Object> {

  private static final Logger LOGGER = LoggerFactory.getLogger(BaseRestClient.class);

  protected final String baseEndpoint;
  protected final HttpClient http;
  protected final ObjectMapper mapper;
  protected final ObjectReader reader;
  protected final URI serverUri;
  protected final Class<T> targetType;

  public BaseRestClient(
      HttpClient http,
      String serverUrl,
      Class<T> targetType,
      ObjectMapper mapper,
      String baseEndpoint) {
    this.baseEndpoint = baseEndpoint;
    this.http = http;
    this.mapper = mapper;
    this.reader = mapper.reader().forType(targetType);
    this.serverUri = URI.create(serverUrl);
    this.targetType = targetType;
  }

  public T create() throws TechnicalException {
    try {
      return targetType.getDeclaredConstructor().newInstance();
    } catch (InstantiationException
        | IllegalAccessException
        | InvocationTargetException
        | NoSuchMethodException e) {
      throw new TechnicalException(
          "Cannot create new instance of " + targetType.getName() + ": " + e, e);
    }
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
            // .header("X-Request-Id", request.getRequestId())
            .build();
    return req;
  }

  public URI createFullUri(String requestUrl) {
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
            // .header("X-Request-Id", request.getRequestId())
            .build();
    return req;
  }

  private HttpRequest createPatchRequest(String requestUrl) {
    final URI url = createFullUri(requestUrl);
    LOGGER.debug("PATCH " + url);
    HttpRequest req =
        HttpRequest.newBuilder()
            .method("PATCH", HttpRequest.BodyPublishers.noBody())
            .uri(url)
            .header("Accept", "application/json")
            // TODO add creation of a request id if needed
            // .header("X-Request-Id", request.getRequestId())
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
            // .header("X-Request-Id", request.getRequestId())
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
            // .header("X-Request-Id", request.getRequestId())
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
            // .header("X-Request-Id", request.getRequestId())
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
            // .header("X-Request-Id", request.getRequestId())
            .build();
    return req;
  }

  protected String doDeleteRequestForString(String requestUrl) throws TechnicalException {
    HttpRequest req = createDeleteRequest(requestUrl);
    try {
      HttpResponse<String> response = http.send(req, HttpResponse.BodyHandlers.ofString());
      Integer statusCode = response.statusCode();
      if (statusCode >= 400) {
        throw HttpErrorDecoder.decode("DELETE " + requestUrl, statusCode, response);
      }
      String body = response.body();
      if (body == null || body.isBlank()) {
        return null;
      }
      String result = mapper.readerFor(String.class).readValue(body);
      return result;
    } catch (InterruptedException | IOException e) {
      throw new TechnicalException("Failed to retrieve response due to connection error", e);
    }
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
      throw new TechnicalException("Failed to retrieve response due to connection error", e);
    }
  }

  protected List<T> doGetRequestForObjectList(String requestUrl) throws TechnicalException {
    return doGetRequestForObjectList(requestUrl, targetType, null);
  }

  protected List doGetRequestForObjectList(String requestUrl, Class<?> targetType)
      throws TechnicalException {
    return doGetRequestForObjectList(requestUrl, targetType, null);
  }

  protected List doGetRequestForObjectList(
      String requestUrl, Class<?> targetType, Filtering filtering) throws TechnicalException {
    if (filtering != null) {
      requestUrl += (requestUrl.contains("?") ? "&" : "?") + getFilterParamsAsString(filtering);
    }
    HttpRequest req = createGetRequest(requestUrl);
    // TODO add creation of a request id if needed
    // .header("X-Request-Id", request.getRequestId())
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
      throw new TechnicalException("Failed to retrieve response due to connection error", e);
    }
  }

  protected PageResponse<T> doGetRequestForPagedObjectList(
      String requestUrl, PageRequest pageRequest) throws TechnicalException {
    if (!requestUrl.contains("?")) {
      requestUrl = requestUrl + "?";
    } else {
      if (!requestUrl.endsWith("&")) {
        requestUrl = requestUrl + "&";
      }
    }
    String findParams = getFindParamsAsString(pageRequest);
    requestUrl = requestUrl + findParams;
    Filtering filtering = pageRequest.getFiltering();
    if (filtering != null) {
      requestUrl += "&" + getFilterParamsAsString(filtering);
    }

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
      PageResponse<T> result = mapper.readerFor(PageResponse.class).readValue(body);
      return result;
    } catch (IOException | InterruptedException e) {
      throw new TechnicalException("Failed to retrieve response due to connection error", e);
    }
  }

  protected PageResponse doGetRequestForPagedObjectList(
      String requestUrl, PageRequest pageRequest, Class<?> targetType) throws TechnicalException {
    return doGetRequestForPagedObjectList(requestUrl, pageRequest);
  }

  protected String doGetRequestForString(String requestUrl) throws TechnicalException {
    HttpRequest req = createGetRequest(requestUrl);
    try {
      HttpResponse<String> response = http.send(req, HttpResponse.BodyHandlers.ofString());
      Integer statusCode = response.statusCode();
      if (statusCode >= 400) {
        throw HttpErrorDecoder.decode("GET " + requestUrl, statusCode, response);
      }
      String body = response.body();
      if (body == null || body.isBlank()) {
        return null;
      }
      String result = mapper.readerFor(String.class).readValue(body);
      return result;
    } catch (IOException | InterruptedException e) {
      throw new TechnicalException("Failed to retrieve response due to connection error", e);
    }
  }

  protected String doPatchRequestForString(String requestUrl) throws TechnicalException {
    HttpRequest req = createPatchRequest(requestUrl);
    try {
      HttpResponse<String> response = http.send(req, HttpResponse.BodyHandlers.ofString());
      Integer statusCode = response.statusCode();
      if (statusCode >= 400) {
        throw HttpErrorDecoder.decode("PATCH " + requestUrl, statusCode, response);
      }
      String body = response.body();
      if (body == null || body.isBlank()) {
        return null;
      }
      String result = mapper.readerFor(String.class).readValue(body);
      return result;
    } catch (InterruptedException | IOException e) {
      throw new TechnicalException("Failed to retrieve response due to connection error", e);
    }
  }

  protected String doPatchRequestForString(String requestUrl, Object object)
      throws TechnicalException {
    try {
      HttpRequest req = createPatchRequest(requestUrl, object);
      HttpResponse<String> response = http.send(req, HttpResponse.BodyHandlers.ofString());
      Integer statusCode = response.statusCode();
      if (statusCode >= 400) {
        throw HttpErrorDecoder.decode("PATCH " + requestUrl, statusCode, response);
      }
      String body = response.body();
      if (body == null || body.isBlank()) {
        return null;
      }
      String result = mapper.readerFor(String.class).readValue(body);
      return result;
    } catch (IOException | InterruptedException e) {
      throw new TechnicalException("Failed to retrieve response due to connection error", e);
    }
  }

  protected T doPostRequestForObject(String requestUrl, T object) throws TechnicalException {
    try {
      HttpRequest req = createPostRequest(requestUrl, object);
      HttpResponse<byte[]> response = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
      Integer statusCode = response.statusCode();
      if (statusCode >= 400) {
        throw HttpErrorDecoder.decode("POST " + requestUrl, statusCode, response);
      }
      // This is the most performant approach for Jackson
      final byte[] body = response.body();
      if (body == null || body.length == 0) {
        return null;
      }
      T result = mapper.readerFor(targetType).readValue(body);
      return result;
    } catch (IOException | InterruptedException e) {
      throw new TechnicalException("Failed to retrieve response due to error", e);
    }
  }

  protected Object doPostRequestForObject(String requestUrl, Object bodyObject, Class<?> targetType)
      throws TechnicalException {
    try {
      HttpRequest req = createPostRequest(requestUrl, bodyObject);
      HttpResponse<byte[]> response = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
      Integer statusCode = response.statusCode();
      if (statusCode >= 400) {
        throw HttpErrorDecoder.decode("POST " + requestUrl, statusCode, response);
      }
      // This is the most performant approach for Jackson
      final byte[] body = response.body();
      if (body == null) {
        return null;
      }
      Object result = mapper.readerFor(targetType).readValue(body);
      return result;
    } catch (IOException | InterruptedException e) {
      throw new TechnicalException("Failed to retrieve response due to error", e);
    }
  }

  protected T doPostRequestForObject(String requestUrl) throws TechnicalException {
    try {
      HttpRequest req = createPostRequest(requestUrl);
      HttpResponse<byte[]> response = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
      Integer statusCode = response.statusCode();
      if (statusCode >= 400) {
        throw HttpErrorDecoder.decode("POST " + requestUrl, statusCode, response);
      }
      final byte[] body = response.body();
      if (body == null || body.length == 0) {
        return null;
      }
      T result = mapper.readerFor(targetType).readValue(body);
      return result;
    } catch (InterruptedException | IOException e) {
      throw new TechnicalException("Failed to retrieve response due to connection error", e);
    }
  }

  protected Object doPostRequestForObject(String requestUrl, Class<?> targetType)
      throws TechnicalException {
    try {
      HttpRequest req = createPostRequest(requestUrl);
      HttpResponse<byte[]> response = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
      Integer statusCode = response.statusCode();
      if (statusCode >= 400) {
        throw HttpErrorDecoder.decode("POST " + requestUrl, statusCode, response);
      }
      // This is the most performant approach for Jackson
      final byte[] body = response.body();
      if (body == null || body.length == 0) {
        return null;
      }
      Object result = mapper.readerFor(targetType).readValue(body);
      return result;
    } catch (IOException | InterruptedException e) {
      throw new TechnicalException("Failed to retrieve response due to error", e);
    }
  }

  protected List<T> doPostRequestForObjectList(String requestUrl, List<T> list)
      throws TechnicalException {
    return (List<T>) doPostRequestForObjectList(requestUrl, (List<Class<?>>) list, targetType);
  }

  protected List<Class<?>> doPostRequestForObjectList(
      String requestUrl, List<Class<?>> list, Class<?> targetType) throws TechnicalException {
    try {
      HttpRequest req = createPostRequest(requestUrl, list);
      HttpResponse<byte[]> response = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
      Integer statusCode = response.statusCode();
      if (statusCode >= 400) {
        throw HttpErrorDecoder.decode("POST " + requestUrl, statusCode, response);
      }
      // This is the most performant approach for Jackson
      final byte[] body = response.body();
      if (body == null || body.length == 0) {
        return null;
      }
      List<Class<?>> result = mapper.readerForListOf(targetType).readValue(body);
      return result;
    } catch (IOException | InterruptedException e) {
      throw new TechnicalException("Failed to retrieve response due to error", e);
    }
  }

  protected String doPostRequestForString(String requestUrl) throws TechnicalException {
    try {
      HttpRequest req = createPostRequest(requestUrl);
      HttpResponse<String> response = http.send(req, HttpResponse.BodyHandlers.ofString());
      Integer statusCode = response.statusCode();
      if (statusCode >= 400) {
        throw HttpErrorDecoder.decode("POST " + requestUrl, statusCode, response);
      }
      String body = response.body();
      if (body == null || body.isBlank()) {
        return null;
      }
      String result = mapper.readerFor(String.class).readValue(body);
      return result;
    } catch (InterruptedException | IOException e) {
      throw new TechnicalException("Failed to retrieve response due to connection error", e);
    }
  }

  protected String doPostRequestForString(String requestUrl, Object object)
      throws TechnicalException {
    try {
      HttpRequest req = createPostRequest(requestUrl, object);
      HttpResponse<String> response = http.send(req, HttpResponse.BodyHandlers.ofString());
      Integer statusCode = response.statusCode();
      if (statusCode >= 400) {
        throw HttpErrorDecoder.decode("POST " + requestUrl, statusCode, response);
      }
      String body = response.body();
      if (body == null || body.isBlank()) {
        return null;
      }
      String result = mapper.readerFor(String.class).readValue(body);
      return result;
    } catch (IOException | InterruptedException e) {
      throw new TechnicalException("Failed to retrieve response due to connection error", e);
    }
  }

  protected T doPutRequestForObject(String requestUrl, T object) throws TechnicalException {
    try {
      HttpRequest req = createPutRequest(requestUrl, object);
      HttpResponse<byte[]> response = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
      Integer statusCode = response.statusCode();
      if (statusCode >= 400) {
        throw HttpErrorDecoder.decode("PUT " + requestUrl, statusCode, response);
      }
      // This is the most performant approach for Jackson
      final byte[] body = response.body();
      if (body == null || body.length == 0) {
        return null;
      }
      T result = mapper.readerFor(targetType).readValue(body);
      return result;
    } catch (IOException | InterruptedException e) {
      throw new TechnicalException("Failed to retrieve response due to connection error", e);
    }
  }

  protected Object doPutRequestForObject(String requestUrl, Object bodyObject, Class<?> targetType)
      throws TechnicalException {
    try {
      HttpRequest req = createPutRequest(requestUrl, bodyObject);
      HttpResponse<byte[]> response = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
      Integer statusCode = response.statusCode();
      if (statusCode >= 400) {
        throw HttpErrorDecoder.decode("PUT " + requestUrl, statusCode, response);
      }
      // This is the most performant approach for Jackson
      final byte[] body = response.body();
      if (body == null) {
        return null;
      }
      Object result = mapper.readerFor(targetType).readValue(body);
      return result;
    } catch (IOException | InterruptedException e) {
      throw new TechnicalException("Failed to retrieve response due to error", e);
    }
  }

  protected List<Class<?>> doPutRequestForObjectList(
      String requestUrl, List<Class<?>> list, Class<?> targetType) throws TechnicalException {
    try {
      HttpRequest req = createPutRequest(requestUrl, list);
      HttpResponse<byte[]> response = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
      Integer statusCode = response.statusCode();
      if (statusCode >= 400) {
        throw HttpErrorDecoder.decode("PUT " + requestUrl, statusCode, response);
      }
      // This is the most performant approach for Jackson
      final byte[] body = response.body();
      if (body == null || body.length == 0) {
        return null;
      }
      List<Class<?>> result = mapper.readerForListOf(targetType).readValue(body);
      return result;
    } catch (IOException | InterruptedException e) {
      throw new TechnicalException("Failed to retrieve response due to error", e);
    }
  }

  protected String doPutRequestForString(String requestUrl, Object object)
      throws TechnicalException {
    try {
      HttpRequest req = createPutRequest(requestUrl, object);
      HttpResponse<String> response = http.send(req, HttpResponse.BodyHandlers.ofString());
      Integer statusCode = response.statusCode();
      if (statusCode >= 400) {
        throw HttpErrorDecoder.decode("PUT " + requestUrl, statusCode, response);
      }
      String body = response.body();
      if (body == null || body.isBlank()) {
        return null;
      }
      String result = mapper.readerFor(String.class).readValue(body);
      return result;
    } catch (IOException | InterruptedException e) {
      throw new TechnicalException("Failed to retrieve response due to connection error", e);
    }
  }

  String filterCriterionToUrlParam(FilterCriterion filterCriterion) {
    String expression = filterCriterion.getExpression();
    boolean isNativeExpression = filterCriterion.isNativeExpression();
    FilterOperation operation = filterCriterion.getOperation();
    if (expression == null || operation == null) {
      return null;
    }
    String operand;
    switch (operation.getOperandCount()) {
      case SINGLEVALUE:
        operand = URLEncoder.encode(filterCriterion.getValue().toString(), StandardCharsets.UTF_8);
        break;
      case MIN_MAX_VALUES:
        operand =
            URLEncoder.encode(filterCriterion.getMinValue().toString(), StandardCharsets.UTF_8)
                + ","
                + URLEncoder.encode(
                    filterCriterion.getMaxValue().toString(), StandardCharsets.UTF_8);
        break;
      case MULTIVALUE:
        operand =
            filterCriterion.getValues().stream()
                .map(value -> URLEncoder.encode(value.toString(), StandardCharsets.UTF_8))
                .collect(Collectors.joining(","))
                .toString();
        break;
      default:
        // no value operations ("set"/"notset")
        operand = "";
        break;
    }

    if (isNativeExpression) {
      expression = URLEncoder.encode("[" + expression + "]", StandardCharsets.UTF_8);
    }
    // expression:operation:operand (non native)
    // [expression]:operation:operand (native)
    return expression + ":" + operation + ":" + operand;
  }

  /**
   * Get the base endpoint path for testing purposes
   *
   * @return the relative base endpoint of the client
   */
  public String getBaseEndpoint() {
    return baseEndpoint;
  }

  /**
   * Converts the given list of filter criterias to a request string
   *
   * @param filterCriterias a list of filter criterias
   * @return the filter criterias as request string
   */
  String getFilterParamsAsString(Filtering filtering) {
    List<FilterCriteria> filterCriterias = filtering.getFilterCriteriaList();
    // braces and logical link operator can be omitted if there is only one AND-linked
    // `FilterCriteria`
    final boolean simpleShape =
        filterCriterias.size() == 1
            && filterCriterias.get(0).getCriterionLink() == FilterLogicalOperator.AND;
    // "{${{logicalLink}};{{criterias}}}"
    final String criteriaTemplate =
        URLEncoder.encode("{", StandardCharsets.UTF_8)
            + "${{logicalLink}};{{criterias}}"
            + URLEncoder.encode("}", StandardCharsets.UTF_8);
    List<String> criteriaStrings = new ArrayList<>();
    for (FilterCriteria criteria : filterCriterias) {
      String criteriaString =
          criteria.stream()
              .filter(Objects::nonNull)
              .map(this::filterCriterionToUrlParam)
              .filter(Objects::nonNull)
              .collect(
                  Collectors.collectingAndThen(
                      Collectors.joining(";"),
                      s ->
                          simpleShape
                              ? s
                              : criteriaTemplate
                                  .replace(
                                      "{{logicalLink}}",
                                      Optional.ofNullable(criteria.getCriterionLink())
                                          .orElse(FilterLogicalOperator.AND)
                                          .getOperand())
                                  .replace("{{criterias}}", s)));
      criteriaStrings.add(criteriaString);
    }
    return "filtering=" + String.join(";", criteriaStrings);
  }

  /**
   * Converts the given pagerequest to a request string
   *
   * @param pageRequest source for find params
   * @return the find params as request string
   */
  String getFindParamsAsString(PageRequest pageRequest) {
    int pageNumber = pageRequest.getPageNumber();
    int pageSize = pageRequest.getPageSize();
    StringBuilder findParams =
        new StringBuilder(String.format("pageNumber=%d&pageSize=%d", pageNumber, pageSize));
    Sorting sorting = pageRequest.getSorting();
    if (sorting == null) {
      return findParams.toString();
    }
    List<Order> orders = sorting.getOrders();
    if (orders == null || orders.isEmpty()) {
      return findParams.toString();
    }
    String sortBy =
        orders.stream()
            .map(
                o -> {
                  String property = o.getProperty();
                  StringBuilder order = new StringBuilder(property);
                  Optional<String> subProperty = o.getSubProperty();
                  if (subProperty.isPresent()) {
                    order.append("_").append(subProperty.get());
                  }
                  Direction direction = o.getDirection();
                  if (direction != null && direction.isDescending()) {
                    order.append(".desc");
                  } else {
                    order.append(".asc");
                  }
                  NullHandling nullHandling = o.getNullHandling();
                  if (nullHandling == NullHandling.NULLS_FIRST) {
                    order.append(".nullsfirst");
                  } else if (nullHandling == NullHandling.NULLS_LAST) {
                    order.append(".nullslast");
                  }
                  boolean ignoreCase = o.isIgnoreCase();
                  if (ignoreCase) {
                    order.append(".ignorecase");
                  }
                  return order.toString();
                })
            .collect(Collectors.joining(","));
    findParams.append("&sortBy=").append(sortBy);
    return findParams.toString();
  }
}
