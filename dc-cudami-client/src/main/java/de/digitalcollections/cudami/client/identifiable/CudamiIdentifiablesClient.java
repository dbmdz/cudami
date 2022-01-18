package de.digitalcollections.cudami.client.identifiable;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.lang.reflect.InvocationTargetException;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiIdentifiablesClient<I extends Identifiable> extends CudamiBaseClient<I> {

  protected final String baseEndpoint;
  private Class<I> typeArgumentClass;

  public CudamiIdentifiablesClient(
      HttpClient http,
      String serverUrl,
      Class<I> identifiableClass,
      ObjectMapper mapper,
      String baseEndpoint) {
    super(http, serverUrl, identifiableClass, mapper);
    this.baseEndpoint = baseEndpoint;
    this.typeArgumentClass = identifiableClass;
  }

  public CudamiIdentifiablesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    this(http, serverUrl, (Class<I>) Identifiable.class, mapper, "/v5/identifiables");
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString(baseEndpoint + "/count"));
  }

  public I create() {
    try {
      return typeArgumentClass.getDeclaredConstructor().newInstance();
    } catch (InstantiationException
        | IllegalAccessException
        | InvocationTargetException
        | NoSuchMethodException e) {
      throw new RuntimeException(
          "Cannot create new instance of " + typeArgumentClass.getName() + ": " + e, e);
    }
  }

  public PageResponse<I> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList(baseEndpoint, pageRequest);
  }

  public SearchPageResponse<I> find(SearchPageRequest searchPageRequest) throws HttpException {
    return doGetSearchRequestForPagedObjectList(baseEndpoint + "/search", searchPageRequest);
  }

  public List<I> find(String searchTerm, int maxResults) throws HttpException {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, 0, maxResults, null);
    SearchPageResponse<I> response = find(searchPageRequest);
    return response.getContent();
  }

  public I findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format(baseEndpoint + "/%s", uuid));
  }

  public I findOne(UUID uuid, Locale locale) throws HttpException {
    return findOne(uuid, locale.toString());
  }

  public I findOne(UUID uuid, String locale) throws HttpException {
    return doGetRequestForObject(String.format(baseEndpoint + "/%s?locale=%s", uuid, locale));
  }

  public I findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(String.format(baseEndpoint + "/%s:%s.json", namespace, id));
  }

  public I save(I identifiable) throws HttpException {
    return doPostRequestForObject(baseEndpoint, identifiable);
  }

  public I update(UUID uuid, I identifiable) throws HttpException {
    return doPutRequestForObject(String.format(baseEndpoint + "/%s", uuid), identifiable);
  }

  /**
   * Get the base endpoint path for testing purposes
   *
   * @return the relative base endpoint of the client
   */
  public String getBaseEndpoint() {
    return baseEndpoint;
  }
}
