package de.digitalcollections.cudami.client;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The CudamiRestClient")
public class CudamiRestClientTest {

  private CudamiRestClient client;
  String serverUrl = "http://localhost:1234/cudami";

  @BeforeEach
  public void beforeEach() {
    client =
        new CudamiRestClient(
            null, serverUrl, Identifiable.class, new DigitalCollectionsObjectMapper(), "/v999");
  }

  @Test
  public void testCreateFullUriWithPath() {

    String requestUrl = "/foo/bar";
    CudamiRestClient base =
        new CudamiRestClient(
            null, serverUrl, Identifiable.class, new DigitalCollectionsObjectMapper(), "/v999");
    URI result = base.createFullUri(requestUrl);
    URI expectedResult = URI.create(serverUrl + requestUrl);
    assertThat(result).isEqualTo(expectedResult);
  }

  @Test
  public void testCreateFullUriWithoutPath() {
    String serverUrl = "http://localhost:1234";
    String requestUrl = "/foo/bar";
    CudamiRestClient base =
        new CudamiRestClient(
            null, serverUrl, Identifiable.class, new DigitalCollectionsObjectMapper(), "/v999");
    URI result = base.createFullUri(requestUrl);
    URI expectedResult = URI.create(serverUrl + requestUrl);
    assertThat(result).isEqualTo(expectedResult);
  }
}
