package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.client.BaseRestClient;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CudamiRestClient<T extends UniqueObject> extends BaseRestClient<T> {

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

  /**
   * @deprecated This method is subject to be removed.
   *     <p>Use {@link CudamiRestClient#deleteByUuid(java.util.UUID)} instead.
   * @param uuid UUID of Object
   */
  @Deprecated(forRemoval = true)
  public void delete(UUID uuid) throws TechnicalException {
    deleteByUuid(uuid);
  }

  public void deleteByUuid(UUID uuid) throws TechnicalException {
    doDeleteRequestForString(String.format("%s/%s", baseEndpoint, uuid));
  }

  @Deprecated(since = "5.0", forRemoval = true)
  /** @deprecated Please use {@link #find(SearchPageRequest)} instead FIXME: Really?! */
  public PageResponse<T> find(PageRequest pageRequest) throws TechnicalException {
    return doGetRequestForPagedObjectList(baseEndpoint, pageRequest);
  }

  public List<T> findAll() throws TechnicalException {
    return doGetRequestForObjectList(baseEndpoint + "/all");
  }

  public T getByUuid(UUID uuid) throws TechnicalException {
    return doGetRequestForObject(String.format("%s/%s", baseEndpoint, uuid));
  }

  public T save(T object) throws TechnicalException {
    return doPostRequestForObject(baseEndpoint, object);
  }

  public T update(UUID uuid, T object) throws TechnicalException {
    return doPutRequestForObject(String.format("%s/%s", baseEndpoint, uuid), object);
  }
}
