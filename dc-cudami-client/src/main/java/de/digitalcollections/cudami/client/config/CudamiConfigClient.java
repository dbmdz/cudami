package de.digitalcollections.cudami.client.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.client.BaseRestClient;
import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.model.exception.http.HttpException;
import java.net.http.HttpClient;

public class CudamiConfigClient extends BaseRestClient<CudamiConfig> {

  public CudamiConfigClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, CudamiConfig.class, mapper, "/v5/config");
  }

  public CudamiConfig getConfig() throws HttpException {
    return (CudamiConfig) doGetRequestForObject(baseEndpoint, CudamiConfig.class);
  }
}
