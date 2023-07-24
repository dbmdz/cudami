package de.digitalcollections.cudami.lobid.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.lobid.client.mapper.Lobid2DCModelMapper;
import de.digitalcollections.lobid.model.LobidPerson;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import java.net.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LobidPersonsClient extends LobidBaseClient<LobidPerson> {

  private static final Logger LOGGER = LoggerFactory.getLogger(LobidPersonsClient.class);

  LobidPersonsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, LobidPerson.class, mapper);
  }

  public Person getByGndId(String gndId) throws TechnicalException {
    LobidPerson lobidPerson = doGetRequestForObject(String.format("/gnd/%s.json", gndId));
    Person person = Lobid2DCModelMapper.mapToPerson(lobidPerson, null);
    return person;
  }
}
