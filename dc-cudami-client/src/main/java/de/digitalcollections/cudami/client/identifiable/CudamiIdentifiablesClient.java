package de.digitalcollections.cudami.client.identifiable;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiIdentifiablesClient extends CudamiBaseClient<Identifiable> {

  public CudamiIdentifiablesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Identifiable.class, mapper);
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/latest/identifiables/count"));
  }

  public Identifiable create() {
    return new Identifiable();
  }

  public PageResponse<Identifiable> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/latest/identifiables", pageRequest);
  }

  public SearchPageResponse<Identifiable> find(SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList("/latest/identifiables/search", searchPageRequest);
  }

  public List<Identifiable> find(String searchTerm, int maxResults) throws HttpException {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, 0, maxResults, null);
    SearchPageResponse<Identifiable> response = find(searchPageRequest);
    return response.getContent();
  }

  public Identifiable findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/latest/identifiables/%s", uuid));
  }

  public Identifiable findOne(UUID uuid, Locale locale) throws HttpException {
    return findOne(uuid, locale.toString());
  }

  public Identifiable findOne(UUID uuid, String locale) throws HttpException {
    return doGetRequestForObject(String.format("/latest/identifiables/%s?locale=%s", uuid, locale));
  }

  public Identifiable findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/latest/identifiables/identifier/%s:%s.json", namespace, id));
  }

  public Identifiable save(Identifiable identifiable) throws HttpException {
    return doPostRequestForObject("/latest/identifiables", identifiable);
  }

  public Identifiable update(UUID uuid, Identifiable identifiable) throws HttpException {
    return doPutRequestForObject(String.format("/latest/identifiables/%s", uuid), identifiable);
  }
}
