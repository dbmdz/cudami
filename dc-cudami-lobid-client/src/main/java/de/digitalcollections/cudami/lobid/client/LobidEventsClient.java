package de.digitalcollections.cudami.lobid.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.lobid.client.mapper.Lobid2DCModelMapper;
import de.digitalcollections.lobid.model.LobidEvent;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Event;
import java.net.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LobidEventsClient extends LobidBaseClient<LobidEvent> {

  private static final Logger LOGGER = LoggerFactory.getLogger(LobidEventsClient.class);

  LobidEventsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, LobidEvent.class, mapper);
  }

  public Event getByGndId(String gndId) throws TechnicalException {
    LobidEvent lobidEvent = doGetRequestForObject(String.format("/gnd/%s.json", gndId));
    Event event = Lobid2DCModelMapper.mapToEvent(lobidEvent, null);
    return event;
  }
}
