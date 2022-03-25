package de.digitalcollections.cudami.admin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiClient;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfigBackend {

  @Value("${cudami.server.url}")
  private String serverUrl;

  @Bean
  public CudamiClient cudamiClient(ObjectMapper objectMapper) {
    final HttpClient http =
        HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    return new CudamiClient(http, serverUrl, objectMapper);
  }
}
