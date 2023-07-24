package de.digitalcollections.cudami.lobid.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.lobid.client.mapper.Lobid2DCModelMapper;
import de.digitalcollections.lobid.model.LobidCorporateBody;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import java.net.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LobidCorporateBodiesClient extends LobidBaseClient<LobidCorporateBody> {

  private static final Logger LOGGER = LoggerFactory.getLogger(LobidCorporateBodiesClient.class);

  LobidCorporateBodiesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, LobidCorporateBody.class, mapper);
  }

  public CorporateBody getByGndId(String gndId) throws TechnicalException {
    LobidCorporateBody lobidCorporateBody =
        doGetRequestForObject(String.format("/gnd/%s.json", gndId));
    CorporateBody corporateBody = Lobid2DCModelMapper.mapToCorporateBody(lobidCorporateBody, null);
    return corporateBody;
  }
}
