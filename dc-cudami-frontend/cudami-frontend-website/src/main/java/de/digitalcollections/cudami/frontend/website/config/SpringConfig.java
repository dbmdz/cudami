package de.digitalcollections.cudami.frontend.website.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import java.net.http.HttpClient;
import java.time.Duration;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SpringConfig {

  @Bean
  public CudamiClient cudamiClient(CudamiConfig cfg, ObjectMapper mapper) {
    return new CudamiClient(
        HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(10))
            .version(HttpClient.Version.HTTP_1_1)
            .build(),
        String.valueOf(cfg.getServer().getUrl()),
        mapper);
  }

  @Bean
  public LayoutDialect layoutDialect() {
    return new LayoutDialect();
  }

  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    DigitalCollectionsObjectMapper objectMapper = new DigitalCollectionsObjectMapper();
    return objectMapper;
  }
}
