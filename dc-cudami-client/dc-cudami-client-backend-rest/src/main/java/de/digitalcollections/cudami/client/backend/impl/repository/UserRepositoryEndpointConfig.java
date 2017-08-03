package de.digitalcollections.cudami.client.backend.impl.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.backend.impl.repository.exceptionhandling.EndpointErrorDecoder;
import feign.Feign;
import feign.gson.GsonEncoder;
import feign.jackson.JacksonDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserRepositoryEndpointConfig {

  @Autowired
  ObjectMapper objectMapper;

  @Bean
  public UserRepositoryEndpoint userRepositoryEndpoint() {
    UserRepositoryEndpoint endpoint = Feign.builder()
            .decoder(new JacksonDecoder(objectMapper))
            .encoder(new GsonEncoder())
            .errorDecoder(new EndpointErrorDecoder())
            .target(UserRepositoryEndpoint.class, "http://localhost:8080");
    return endpoint;
  }
}
