package de.digitalcollections.cudami.lobid.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.lobid.client.mapper.Lobid2DCModelMapper;
import de.digitalcollections.lobid.model.LobidGeoLocation;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import java.net.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LobidHumanSettlementsClient extends LobidBaseClient<LobidGeoLocation> {

  private static final Logger LOGGER = LoggerFactory.getLogger(LobidHumanSettlementsClient.class);

  LobidHumanSettlementsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, LobidGeoLocation.class, mapper);
  }

  public HumanSettlement getByGndId(String gndId) throws TechnicalException {
    LobidGeoLocation lobidGeoLocation = doGetRequestForObject(String.format("/gnd/%s.json", gndId));
    HumanSettlement humanSettlement =
        Lobid2DCModelMapper.mapToHumanSettlement(lobidGeoLocation, null);
    return humanSettlement;
  }
}
