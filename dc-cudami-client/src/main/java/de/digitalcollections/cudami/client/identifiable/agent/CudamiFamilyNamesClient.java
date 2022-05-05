package de.digitalcollections.cudami.client.identifiable.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifiablesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.agent.FamilyName;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.net.http.HttpClient;

public class CudamiFamilyNamesClient extends CudamiIdentifiablesClient<FamilyName> {

  public CudamiFamilyNamesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, FamilyName.class, mapper, "/v5/familynames");
  }

  @Override
  public PageResponse<FamilyName> find(PageRequest pageRequest) throws TechnicalException {
    return doGetSearchRequestForPagedObjectList(baseEndpoint, pageRequest);
  }
}
