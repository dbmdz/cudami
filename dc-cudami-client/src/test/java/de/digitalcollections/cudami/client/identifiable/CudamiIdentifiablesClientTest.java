package de.digitalcollections.cudami.client.identifiable;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifiablesClientTest.CudamiIdentifiablesClientForIdentifiables;
import de.digitalcollections.model.identifiable.Identifiable;
import java.net.http.HttpClient;
import org.junit.jupiter.api.DisplayName;

@DisplayName("The Identifiables Client for Identifiables")
public class CudamiIdentifiablesClientTest
    extends BaseCudamiIdentifiablesClientTest<
        Identifiable, CudamiIdentifiablesClientForIdentifiables> {

  // ------------------------------------------
  // We must create a generics-free client, since we cannot use generics with the extended
  // BaseCudamiIdentifiablesClientTest as second type argument
  public static class CudamiIdentifiablesClientForIdentifiables
      extends CudamiIdentifiablesClient<Identifiable> {

    public CudamiIdentifiablesClientForIdentifiables(
        HttpClient httpClient, String serverUrl, ObjectMapper objectMapper) {
      super(httpClient, serverUrl, Identifiable.class, objectMapper, "/v5/identifiable");
    }
  }
}
