package de.digitalcollections.cudami.client.identifiable.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifiablesClient;
import de.digitalcollections.model.exception.http.HttpException;
import de.digitalcollections.model.identifiable.agent.FamilyName;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.net.http.HttpClient;

public class CudamiFamilyNamesClient extends CudamiIdentifiablesClient<FamilyName> {

  public CudamiFamilyNamesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, FamilyName.class, mapper, "/v5/familynames");
  }

  @Override
  public SearchPageResponse<FamilyName> find(SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList(baseEndpoint, searchPageRequest);
  }
}
