package de.digitalcollections.cudami.client.identifiable.entity.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEntitiesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;

public class CudamiCorporateBodiesClient extends CudamiEntitiesClient<CorporateBody> {

  public CudamiCorporateBodiesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, CorporateBody.class, mapper, "/v5/corporatebodies");
  }

  public CorporateBody fetchAndSaveByGndId(String gndId) throws TechnicalException {
    return doPostRequestForObject(String.format("%s/gnd/%s", baseEndpoint, gndId));
  }

  @Override
  public SearchPageResponse<CorporateBody> find(SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList(baseEndpoint, searchPageRequest);
  }

  public List<Locale> getLanguages() throws TechnicalException {
    return doGetRequestForObjectList(String.format("%s/languages", baseEndpoint), Locale.class);
  }
}
