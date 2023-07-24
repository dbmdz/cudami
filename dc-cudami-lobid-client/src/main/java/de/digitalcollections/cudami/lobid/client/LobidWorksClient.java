package de.digitalcollections.cudami.lobid.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.lobid.client.mapper.Lobid2DCModelMapper;
import de.digitalcollections.lobid.model.LobidWork;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.work.Work;
import java.net.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LobidWorksClient extends LobidBaseClient<LobidWork> {

  private static final Logger LOGGER = LoggerFactory.getLogger(LobidWorksClient.class);

  LobidWorksClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, LobidWork.class, mapper);
  }

  public Work getByGndId(String gndId) throws TechnicalException {
    LobidWork lobidWork = doGetRequestForObject(String.format("/gnd/%s.json", gndId));
    Work work = Lobid2DCModelMapper.mapToWork(lobidWork, null);
    return work;
  }
}
