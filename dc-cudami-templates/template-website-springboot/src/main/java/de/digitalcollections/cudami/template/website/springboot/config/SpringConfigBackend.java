package de.digitalcollections.cudami.template.website.springboot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiClientBuilder;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SpringConfigBackend {

  @Value("${cudami.server.url}")
  String serverUrl;

  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    return new DigitalCollectionsObjectMapper();
  }

  @Bean
  public CudamiClient cudamiClient() {
    CudamiClientBuilder builder = new CudamiClientBuilder(serverUrl);
    return builder.build();
  }
}
