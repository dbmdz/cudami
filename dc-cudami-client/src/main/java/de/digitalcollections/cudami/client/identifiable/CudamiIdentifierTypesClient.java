package de.digitalcollections.cudami.client.identifiable;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.identifiable.IdentifierType;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;

public class CudamiIdentifierTypesClient extends CudamiBaseClient<IdentifierType> {

  public CudamiIdentifierTypesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, IdentifierType.class, mapper);
  }

  public IdentifierType create() {
    return new IdentifierType();
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/latest/identifiertypes/count"));
  }

  public PageResponse<IdentifierType> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/latest/identifiertypes", pageRequest);
  }

  public SearchPageResponse<IdentifierType> find(SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList(
        "/latest/identifiertypes/search", searchPageRequest);
  }

  public List<IdentifierType> find(String searchTerm, int maxResults) throws HttpException {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, 0, maxResults, null);
    SearchPageResponse<IdentifierType> response = find(searchPageRequest);
    return response.getContent();
  }

  public IdentifierType findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/latest/identifiertypes/%s", uuid));
  }

  public IdentifierType findOne(UUID uuid, String locale) throws HttpException {
    return doGetRequestForObject(
        String.format("/latest/identifiertypes/%s?locale=%s", uuid, locale));
  }

  public IdentifierType findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/latest/identifiertypes/identifier/%s:%s.json", namespace, id));
  }

  public IdentifierType save(IdentifierType identifierType) throws HttpException {
    return doPostRequestForObject("/latest/identifiertypes", (IdentifierType) identifierType);
  }

  public IdentifierType update(UUID uuid, IdentifierType identifierType) throws HttpException {
    return doPutRequestForObject(
        String.format("/latest/identifiertypes/%s", uuid), (IdentifierType) identifierType);
  }
}
