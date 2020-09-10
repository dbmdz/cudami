package de.digitalcollections.cudami.lobid.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.lobid.client.exceptions.HttpException;
import de.digitalcollections.cudami.lobid.client.model.LobidCorporateBody;
import de.digitalcollections.model.api.identifiable.entity.Corporation;
import java.net.http.HttpClient;

public class LobidCorporationsClient extends LobidBaseClient<LobidCorporateBody> {

  LobidCorporationsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, LobidCorporateBody.class, mapper);
  }

  public Corporation getByGndId(String gndId) throws HttpException {
    LobidCorporateBody lobidCorporateBody =
        doGetRequestForObject(String.format("/gnd/%s.json", gndId));
    Corporation corporation = mapLobidToModel(lobidCorporateBody);
    return corporation;
  }

  private Corporation mapLobidToModel(LobidCorporateBody lobidCorporateBody) {
    throw new UnsupportedOperationException(
        "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
  }
}
