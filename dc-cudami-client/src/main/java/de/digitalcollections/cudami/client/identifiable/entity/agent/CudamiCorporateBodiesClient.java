package de.digitalcollections.cudami.client.identifiable.entity.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEntitiesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;

public class CudamiCorporateBodiesClient extends CudamiEntitiesClient<CorporateBody> {

  public CudamiCorporateBodiesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, CorporateBody.class, mapper, API_VERSION_PREFIX + "/corporatebodies");
  }

  public CorporateBody fetchAndSaveByGndId(String gndId) throws TechnicalException {
    return doPostRequestForObject(String.format("%s/gnd/%s", baseEndpoint, gndId));
  }

  public List<Locale> getLanguages() throws TechnicalException {
    return doGetRequestForObjectList(String.format("%s/languages", baseEndpoint), Locale.class);
  }
}
