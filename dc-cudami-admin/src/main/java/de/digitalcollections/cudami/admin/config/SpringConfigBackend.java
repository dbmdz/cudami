package de.digitalcollections.cudami.admin.config;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfigBackend {

  @Value("${cudami.server.url}")
  private String serverUrl;

  @Bean
  public CudamiClient cudamiClient() {
    return new CudamiClient(serverUrl, new DigitalCollectionsObjectMapper());
  }
}
