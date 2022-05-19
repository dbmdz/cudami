package de.digitalcollections.cudami.client.config;

import static de.digitalcollections.cudami.client.CudamiRestClient.API_VERSION_PREFIX;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.client.BaseRestClient;
import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.model.exception.TechnicalException;
import java.net.http.HttpClient;

public class CudamiConfigClient extends BaseRestClient<CudamiConfig> {

  public CudamiConfigClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, CudamiConfig.class, mapper, API_VERSION_PREFIX + "/config");
  }

  public CudamiConfig getConfig() throws TechnicalException {
    return (CudamiConfig) doGetRequestForObject(baseEndpoint, CudamiConfig.class);
  }
}
