package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.IdentifierType;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifierTypeImpl;
import de.digitalcollections.model.impl.paging.SearchPageRequestImpl;
import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;

public class CudamiIdentifierTypesClient extends CudamiBaseClient<IdentifierTypeImpl> {

  public CudamiIdentifierTypesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, IdentifierTypeImpl.class, mapper);
  }

  public IdentifierType create() {
    return new IdentifierTypeImpl();
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/latest/identifiertypes/count"));
  }

  public PageResponse<IdentifierTypeImpl> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/latest/identifiertypes", pageRequest);
  }

  public SearchPageResponse<IdentifierTypeImpl> find(SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList(
        "/latest/identifiertypes/search", searchPageRequest);
  }

  public List<IdentifierTypeImpl> find(String searchTerm, int maxResults) throws HttpException {
    SearchPageRequest searchPageRequest =
        new SearchPageRequestImpl(searchTerm, 0, maxResults, null);
    SearchPageResponse<IdentifierTypeImpl> response = find(searchPageRequest);
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
    return doPostRequestForObject("/latest/identifiertypes", (IdentifierTypeImpl) identifierType);
  }

  public IdentifierType update(UUID uuid, IdentifierType identifierType) throws HttpException {
    return doPutRequestForObject(
        String.format("/latest/identifiertypes/%s", uuid), (IdentifierTypeImpl) identifierType);
  }
}
