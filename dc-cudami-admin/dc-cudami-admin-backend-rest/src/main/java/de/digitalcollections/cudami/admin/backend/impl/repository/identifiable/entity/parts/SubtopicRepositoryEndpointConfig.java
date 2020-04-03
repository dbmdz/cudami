package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity.parts;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.commons.feign.codec.EndpointErrorDecoder;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SubtopicRepositoryEndpointConfig {

  @Value(value = "${cudami.server.address}")
  private String cudamiServerAddress;

  @Autowired ObjectMapper objectMapper;

  @Bean
  public SubtopicRepositoryEndpoint subtopicRepositoryEndpoint() {
    SubtopicRepositoryEndpoint endpoint =
        Feign.builder()
            .decoder(new JacksonDecoder(objectMapper))
            .encoder(new JacksonEncoder(objectMapper))
            .errorDecoder(new EndpointErrorDecoder())
            .target(SubtopicRepositoryEndpoint.class, cudamiServerAddress);
    return endpoint;
  }
}
