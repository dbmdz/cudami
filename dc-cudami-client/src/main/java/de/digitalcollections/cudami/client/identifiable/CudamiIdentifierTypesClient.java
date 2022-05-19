package de.digitalcollections.cudami.client.identifiable;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiRestClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.IdentifierType;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;

public class CudamiIdentifierTypesClient extends CudamiRestClient<IdentifierType> {

  public CudamiIdentifierTypesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, IdentifierType.class, mapper, API_VERSION_PREFIX + "/identifiertypes");
  }

  public List<IdentifierType> find(String searchTerm, int maxResults) throws TechnicalException {
    PageRequest pageRequest = new PageRequest(searchTerm, 0, maxResults, null);
    PageResponse<IdentifierType> response = find(pageRequest);
    return response.getContent();
  }

  public IdentifierType getByUuidAndLocale(UUID uuid, String locale) throws TechnicalException {
    return doGetRequestForObject(String.format(baseEndpoint + "/%s?locale=%s", uuid, locale));
  }
}
