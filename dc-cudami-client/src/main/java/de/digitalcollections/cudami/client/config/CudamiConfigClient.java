package de.digitalcollections.cudami.client.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.cudami.model.config.CudamiConfig;
import java.net.http.HttpClient;

public class CudamiConfigClient extends CudamiBaseClient<CudamiConfig> {

  public CudamiConfigClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, CudamiConfig.class, mapper);
  }

  public CudamiConfig getConfig() throws HttpException {
    return (CudamiConfig) doGetRequestForObject("/v5/config", CudamiConfig.class);
  }
}
