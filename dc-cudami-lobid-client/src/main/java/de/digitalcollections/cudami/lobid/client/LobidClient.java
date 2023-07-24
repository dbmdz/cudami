package de.digitalcollections.cudami.lobid.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.lobid.jackson.LobidObjectMapper;
import java.net.http.HttpClient;
import java.time.Duration;

public class LobidClient {

  protected final HttpClient http;
  private final LobidCorporateBodiesClient lobidCorporateBodiesClient;
  private final LobidEventsClient lobidEventsClient;
  private final LobidGeoLocationsClient lobidGeoLocationsClient;
  private final LobidHumanSettlementsClient lobidHumanSettlementsClient;
  private final LobidPersonsClient lobidPersonsClient;
  private final LobidSubjectsClient lobidSubjectsClient;
  private final LobidWorksClient lobidWorksClient;

  public LobidClient() {
    this("https://lobid.org", new LobidObjectMapper());
  }

  public LobidClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    this.http = http;
    this.lobidCorporateBodiesClient = new LobidCorporateBodiesClient(http, serverUrl, mapper);
    this.lobidEventsClient = new LobidEventsClient(http, serverUrl, mapper);
    this.lobidGeoLocationsClient = new LobidGeoLocationsClient(http, serverUrl, mapper);
    this.lobidHumanSettlementsClient = new LobidHumanSettlementsClient(http, serverUrl, mapper);
    this.lobidPersonsClient = new LobidPersonsClient(http, serverUrl, mapper);
    this.lobidSubjectsClient = new LobidSubjectsClient(http, serverUrl, mapper);
    this.lobidWorksClient = new LobidWorksClient(http, serverUrl, mapper);
  }

  public LobidClient(String serverUrl, ObjectMapper mapper) {
    this(
        HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(10))
            .build(),
        serverUrl,
        mapper);
  }

  public LobidCorporateBodiesClient forCorporateBodies() {
    return lobidCorporateBodiesClient;
  }

  public LobidEventsClient forEvents() {
    return lobidEventsClient;
  }

  public LobidGeoLocationsClient forGeoLocations() {
    return lobidGeoLocationsClient;
  }

  public LobidHumanSettlementsClient forHumanSettlements() {
    return lobidHumanSettlementsClient;
  }

  public LobidPersonsClient forPersons() {
    return lobidPersonsClient;
  }

  public LobidSubjectsClient forSubjects() {
    return lobidSubjectsClient;
  }

  public LobidWorksClient forWorks() {
    return lobidWorksClient;
  }
}
