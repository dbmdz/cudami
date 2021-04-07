package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifiableImpl;
import de.digitalcollections.model.impl.paging.SearchPageRequestImpl;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiIdentifiablesClient extends CudamiBaseClient<IdentifiableImpl> {

  public CudamiIdentifiablesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, IdentifiableImpl.class, mapper);
  }

  public Identifiable create() {
    return new IdentifiableImpl();
  }

  public long count() throws HttpException {
    // No GET endpoint for /latest/identifiables/count available!
    throw new HttpException("/latest/identifiables/count", 404);
  }

  public PageResponse<IdentifiableImpl> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/v2/identifiables", pageRequest);
  }

  public SearchPageResponse<IdentifiableImpl> find(SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList("/v2/identifiables/search", searchPageRequest);
  }

  public List<IdentifiableImpl> find(String searchTerm, int maxResults) throws HttpException {
    SearchPageRequest searchPageRequest =
        new SearchPageRequestImpl(searchTerm, 0, maxResults, null);
    SearchPageResponse<IdentifiableImpl> response = find(searchPageRequest);
    return response.getContent();
  }

  public Identifiable findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v2/identifiables/%s", uuid));
  }

  public Identifiable findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/v2/identifiables/identifier/%s:%s.json", namespace, id));
  }

  public Identifiable findOne(UUID uuid, Locale locale) throws HttpException {
    return findOne(uuid, locale.toString());
  }

  public Identifiable findOne(UUID uuid, String locale) throws HttpException {
    return doGetRequestForObject(String.format("/v2/identifiables/%s?locale=%s", uuid, locale));
  }

  public Identifiable save(Identifiable identifiable) throws HttpException {
    // No POST endpoint for /latest/fileresources/search available!
    throw new HttpException("/latest/identifiables", 404);
  }

  public Identifiable update(UUID uuid, Identifiable identifiable) throws HttpException {
    // No PUT endpoint for /latest/fileresources/search available!
    throw new HttpException(String.format("/latest/identifiables/%s", uuid), 404);
  }
}
