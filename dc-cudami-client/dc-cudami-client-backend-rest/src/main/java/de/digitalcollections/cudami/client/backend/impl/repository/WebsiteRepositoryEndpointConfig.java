package de.digitalcollections.cudami.client.backend.impl.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.backend.impl.repository.exceptionhandling.EndpointErrorDecoder;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebsiteRepositoryEndpointConfig {

  @Autowired
  ObjectMapper objectMapper;

  @Bean
  public WebsiteRepositoryEndpoint websiteRepositoryEndpoint() {
    WebsiteRepositoryEndpoint endpoint = Feign.builder()
            .decoder(new JacksonDecoder(objectMapper))
            .encoder(new JacksonEncoder(objectMapper))
            .errorDecoder(new EndpointErrorDecoder())
            .target(WebsiteRepositoryEndpoint.class, "http://localhost:8080");
    return endpoint;
  }
}
