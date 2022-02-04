package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.client.BaseRestClient;
import de.digitalcollections.model.exception.http.HttpException;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.lang.reflect.InvocationTargetException;
import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CudamiRestClient<T extends Object> extends BaseRestClient<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CudamiRestClient.class);

  public CudamiRestClient(
      HttpClient http,
      String serverUrl,
      Class<T> targetType,
      ObjectMapper mapper,
      String baseEndpoint) {
    super(http, serverUrl, targetType, mapper, baseEndpoint);
  }

  public long count() throws HttpException {
    String result = doGetRequestForString(baseEndpoint + "/count");
    return Long.parseLong(result);
  }

  public T create() {
    try {
      return targetType.getDeclaredConstructor().newInstance();
    } catch (InstantiationException
        | IllegalAccessException
        | InvocationTargetException
        | NoSuchMethodException e) {
      throw new RuntimeException(
          "Cannot create new instance of " + targetType.getName() + ": " + e, e);
    }
  }

  /**
   * @deprecated This method is subject to be removed.
   *     <p>Use {@link CudamiRestClient#deleteByUuid(java.util.UUID)} instead.
   * @param uuid UUID of Object
   */
  @Deprecated(forRemoval = true)
  public void delete(UUID uuid) throws HttpException {
    deleteByUuid(uuid);
  }

  public void deleteByUuid(UUID uuid) throws HttpException {
    doDeleteRequestForString(String.format("%s/%s", baseEndpoint, uuid));
  }

  @Deprecated(since = "5.0", forRemoval = true)
  /** @deprecated Please use {@link #find(SearchPageRequest)} instead FIXME: Really?! */
  public PageResponse<T> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList(baseEndpoint, pageRequest);
  }

  public List<T> findAll() throws HttpException {
    return doGetRequestForObjectList(baseEndpoint + "/all");
  }

  /**
   * @deprecated This method is subject to be removed.
   *     <p>Use {@link CudamiRestClient#getByUuid(java.util.UUID)} instead.
   * @param uuid UUID of Object
   * @return object with given UUID
   */
  @Deprecated(forRemoval = true)
  public T findOne(UUID uuid) throws HttpException {
    return getByUuid(uuid);
  }

  public T getByUuid(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("%s/%s", baseEndpoint, uuid));
  }

  public List<FileResource> getRelatedFileResources(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/v5/identifiables/%s/related/fileresources", uuid), FileResource.class);
  }

  public T save(T object) throws HttpException {
    return doPostRequestForObject(baseEndpoint, object);
  }

  public T update(UUID uuid, T object) throws HttpException {
    return doPutRequestForObject(String.format("%s/%s", baseEndpoint, uuid), object);
  }
}
