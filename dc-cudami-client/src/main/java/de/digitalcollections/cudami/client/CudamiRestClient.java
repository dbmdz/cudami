package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.client.BaseRestClient;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.exception.http.client.ResourceNotFoundException;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.net.http.HttpClient;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CudamiRestClient<T extends UniqueObject> extends BaseRestClient<T> {

  public static final String API_VERSION_PREFIX = "/v6";
  private static final Logger LOGGER = LoggerFactory.getLogger(CudamiRestClient.class);

  public CudamiRestClient(
      HttpClient http,
      String serverUrl,
      Class<T> targetType,
      ObjectMapper mapper,
      String baseEndpoint) {
    super(http, serverUrl, targetType, mapper, baseEndpoint);
  }

  public long count() throws TechnicalException {
    String result = doGetRequestForString(baseEndpoint + "/count");
    return Long.parseLong(result);
  }

  public void deleteByUuid(UUID uuid) throws TechnicalException {
    try {
      doDeleteRequestForString(String.format("%s/%s", baseEndpoint, uuid));
    } catch (ResourceNotFoundException e) {
    }
  }

  public PageResponse<T> find(PageRequest pageRequest) throws TechnicalException {
    return doGetRequestForPagedObjectList(baseEndpoint, pageRequest);
  }

  public T getByUuid(UUID uuid) throws TechnicalException {
    return getByUuid(uuid, null);
  }

  public T getByUuid(UUID uuid, Map<String, String> additionalParameters)
      throws TechnicalException {
    try {
      String params = "";
      if (additionalParameters != null && !additionalParameters.isEmpty()) {
        params =
            additionalParameters.entrySet().stream()
                .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("&", "?", ""));
      }
      return doGetRequestForObject(String.format("%s/%s%s", baseEndpoint, uuid, params));
    } catch (ResourceNotFoundException e) {
      return null;
    }
  }

  public T save(T object) throws TechnicalException {
    return doPostRequestForObject(baseEndpoint, object);
  }

  public T update(UUID uuid, T object) throws TechnicalException {
    return doPutRequestForObject(String.format("%s/%s", baseEndpoint, uuid), object);
  }
}
