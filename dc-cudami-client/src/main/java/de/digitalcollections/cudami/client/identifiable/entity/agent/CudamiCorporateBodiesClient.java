package de.digitalcollections.cudami.client.identifiable.entity.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifiablesClient;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;

public class CudamiCorporateBodiesClient extends CudamiIdentifiablesClient<CorporateBody> {
  public CudamiCorporateBodiesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, CorporateBody.class, mapper, "/v5/corporatebodies");
  }

  public CorporateBody fetchAndSaveByGndId(String gndId) throws HttpException {
    return doPostRequestForObject(String.format(baseEndpoint + "/gnd/%s", gndId));
  }

  // TODO: Implement with test
  @Deprecated(since = "5.0", forRemoval = true)
  /** @deprecated Please use {@link #find(SearchPageRequest)} instead */
  public PageResponse<CorporateBody> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList(baseEndpoint, pageRequest);
  }

  public List<Locale> getLanguages() throws HttpException {
    return doGetRequestForObjectList(baseEndpoint + "/languages", Locale.class);
  }
}
