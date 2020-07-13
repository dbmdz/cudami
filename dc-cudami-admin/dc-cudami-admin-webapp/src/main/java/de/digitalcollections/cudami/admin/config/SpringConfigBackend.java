package de.digitalcollections.cudami.admin.config;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiCollectionsClient;
import de.digitalcollections.cudami.client.CudamiCorporationsClient;
import de.digitalcollections.cudami.client.CudamiProjectsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfigBackend {

  @Value("${cudami.server.url}")
  private String serverUrl;

  @Bean
  public CudamiClient cudamiClient() {
    return new CudamiClient(serverUrl);
  }

  @Bean
  public CudamiCollectionsClient cudamiCollectionsClient() {
    return CudamiCollectionsClient.build(serverUrl);
  }

  @Bean
  public CudamiCorporationsClient cudamiCorporationsClient() {
    return CudamiCorporationsClient.build(serverUrl);
  }

  @Bean
  public CudamiProjectsClient cudamiProjectsClient() {
    return CudamiProjectsClient.build(serverUrl);
  }
}
