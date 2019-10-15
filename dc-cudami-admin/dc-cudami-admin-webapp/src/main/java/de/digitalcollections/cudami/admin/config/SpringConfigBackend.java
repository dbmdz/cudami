package de.digitalcollections.cudami.admin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import de.digitalcollections.cudami.client.CudamiCollectionsClient;

@Configuration
public class SpringConfigBackend {

  @Value("${cudami.server.url}")
  String serverUrl;

  @Bean
  public CudamiCollectionsClient cudamiCollectionsClient() {
    return CudamiCollectionsClient.build(serverUrl);
  }
}
