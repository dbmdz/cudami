package de.digitalcollections.cudami.template.website.springboot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.spring.config.SpringConfigCudami;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@Configuration
@Import(SpringConfigCudami.class)
public class SpringConfigBackend {

  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    return new DigitalCollectionsObjectMapper();
  }
}
