package de.digitalcollections.cudami.client.identifiable;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiRestClient;
import de.digitalcollections.model.exception.http.HttpException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.paging.Direction;
import de.digitalcollections.model.paging.NullHandling;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.Sorting;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiIdentifiablesClient<I extends Identifiable> extends CudamiRestClient<I> {

  public CudamiIdentifiablesClient(
      HttpClient http,
      String serverUrl,
      Class<I> identifiableClass,
      ObjectMapper mapper,
      String baseEndpoint) {
    super(http, serverUrl, identifiableClass, mapper, baseEndpoint);
  }

  public CudamiIdentifiablesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, (Class<I>) Identifiable.class, mapper, "/v5/identifiables");
  }

  public SearchPageResponse<I> find(SearchPageRequest searchPageRequest) throws HttpException {
    return doGetSearchRequestForPagedObjectList(baseEndpoint + "/search", searchPageRequest);
  }

  public List<I> find(String searchTerm, int maxResults) throws HttpException {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, 0, maxResults, null);
    SearchPageResponse<I> response = find(searchPageRequest);
    return response.getContent();
  }

  public PageResponse<I> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format(baseEndpoint + "?language=%s&initial=%s", language, initial), pageRequest);
  }

  public PageResponse<I> findByLanguageAndInitial(
      int pageNumber,
      int pageSize,
      String sortField,
      String sortDirection,
      String nullHandling,
      String language,
      String initial)
      throws HttpException {
    Order order =
        new Order(
            Direction.fromString(sortDirection), sortField, NullHandling.valueOf(nullHandling));
    Sorting sorting = new Sorting(order);
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize, sorting);
    return findByLanguageAndInitial(pageRequest, language, initial);
  }

  /**
   * @deprecated This method is subject to be removed.
   *     <p>Use {@link CudamiIdentifiablesClient#getByUuidAndLocale(java.util.UUID,
   *     java.util.Locale)} instead.
   * @param uuid unique uuid of object
   * @param locale locale of localized texts of object
   * @return object with given uuid and locale
   */
  @Deprecated(forRemoval = true)
  public I findOne(UUID uuid, Locale locale) throws HttpException {
    return getByUuidAndLocale(uuid, locale);
  }

  /**
   * @deprecated This method is subject to be removed.
   *     <p>Use {@link CudamiIdentifiablesClient#getByUuidAndLocale(java.util.UUID,
   *     java.lang.String)} instead.
   * @param uuid unique uuid of object
   * @param locale locale of localized texts of object
   * @return object with given uuid and locale
   */
  @Deprecated(forRemoval = true)
  public I findOne(UUID uuid, String locale) throws HttpException {
    return getByUuidAndLocale(uuid, locale);
  }

  /**
   * @deprecated This method is subject to be removed.
   *     <p>Use {@link CudamiIdentifiablesClient#getByIdentifier(java.lang.String,
   *     java.lang.String)} instead.
   * @param namespace namespace part of identifier
   * @param id id part of identifier
   * @return object with given identifier
   */
  @Deprecated(forRemoval = true)
  public I findOneByIdentifier(String namespace, String id) throws HttpException {
    return getByIdentifier(namespace, id);
  }

  public I getByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format(baseEndpoint + "/identifier/%s:%s.json", namespace, id));
  }

  public I getByUuidAndLocale(UUID uuid, Locale locale) throws HttpException {
    return getByUuidAndLocale(uuid, locale.toString());
  }

  public I getByUuidAndLocale(UUID uuid, String locale) throws HttpException {
    return doGetRequestForObject(String.format(baseEndpoint + "/%s?locale=%s", uuid, locale));
  }

  public List<FileResource> getRelatedFileResources(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/v5/identifiables/%s/related/fileresources", uuid), FileResource.class);
  }
}
