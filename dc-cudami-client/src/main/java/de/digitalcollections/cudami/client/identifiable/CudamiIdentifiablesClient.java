package de.digitalcollections.cudami.client.identifiable;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiRestClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.NullHandling;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
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
    super(
        http,
        serverUrl,
        (Class<I>) Identifiable.class,
        mapper,
        API_VERSION_PREFIX + "/identifiables");
  }

  public List<I> find(String searchTerm, int maxResults) throws TechnicalException {
    PageRequest pageRequest = new PageRequest(searchTerm, 0, maxResults, null);
    PageResponse<I> response = find(pageRequest);
    return response.getContent();
  }

  public PageResponse<I> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) throws TechnicalException {
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
      throws TechnicalException {
    Order order =
        new Order(
            Direction.fromString(sortDirection), sortField, NullHandling.valueOf(nullHandling));
    Sorting sorting = new Sorting(order);
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize, sorting);
    return findByLanguageAndInitial(pageRequest, language, initial);
  }

  public I getByIdentifier(String namespace, String id) throws TechnicalException {
    return doGetRequestForObject(
        String.format(baseEndpoint + "/identifier/%s:%s.json", namespace, id));
  }

  public I getByUuidAndLocale(UUID uuid, Locale locale) throws TechnicalException {
    return getByUuidAndLocale(uuid, locale.toString());
  }

  public I getByUuidAndLocale(UUID uuid, String locale) throws TechnicalException {
    return doGetRequestForObject(String.format(baseEndpoint + "/%s?locale=%s", uuid, locale));
  }

  public LocalizedUrlAliases getLocalizedUrlAliases(UUID uuid) throws TechnicalException {
    return (LocalizedUrlAliases)
        doGetRequestForObject(
            String.format(baseEndpoint + "/%s/localizedUrlAliases", uuid),
            LocalizedUrlAliases.class);
  }
}
