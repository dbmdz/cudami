package de.digitalcollections.cudami.lobid.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.lobid.client.model.jackson.LobidObjectMapper;
import java.net.http.HttpClient;
import java.time.Duration;

public class LobidClient {

  protected final HttpClient http;
  private final LobidCorporateBodiesClient lobidCorporateBodiesClient;

  public LobidClient() {
    this("https://lobid.org", new LobidObjectMapper());
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

  public LobidClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    this.http = http;
    this.lobidCorporateBodiesClient = new LobidCorporateBodiesClient(http, serverUrl, mapper);
  }

  public LobidCorporateBodiesClient forCorporateBodies() {
    return lobidCorporateBodiesClient;
  }
}
