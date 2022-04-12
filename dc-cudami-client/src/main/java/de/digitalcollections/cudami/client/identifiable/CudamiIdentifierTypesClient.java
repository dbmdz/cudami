package de.digitalcollections.cudami.client.identifiable;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiRestClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.IdentifierType;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;

public class CudamiIdentifierTypesClient extends CudamiRestClient<IdentifierType> {

  public CudamiIdentifierTypesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, IdentifierType.class, mapper, "/v5/identifiertypes");
  }

  public SearchPageResponse<IdentifierType> find(SearchPageRequest searchPageRequest)
      throws TechnicalException {
    return doGetSearchRequestForPagedObjectList(baseEndpoint + "/search", searchPageRequest);
  }

  public List<IdentifierType> find(String searchTerm, int maxResults) throws TechnicalException {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, 0, maxResults, null);
    SearchPageResponse<IdentifierType> response = find(searchPageRequest);
    return response.getContent();
  }

  public IdentifierType getByUuidAndLocale(UUID uuid, String locale) throws TechnicalException {
    return doGetRequestForObject(String.format(baseEndpoint + "/%s?locale=%s", uuid, locale));
  }
}
