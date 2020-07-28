package de.digitalcollections.cudami.template.website.springboot;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ComponentScan(
    basePackages = {
      "de.digitalcollections.cudami.template.website.springboot.controller",
      "de.digitalcollections.cudami.template.website.springboot.business",
      "de.digitalcollections.cudami.template.website.springboot.repository"
    })
public class TestConfig {

  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    return new DigitalCollectionsObjectMapper();
  }

  @Bean
  public CudamiLocalesClient cudamiLocalesClient(ObjectMapper objectMapper) {
    return new TestCudamiLocalesClient("http://localhost", objectMapper);
  }

  public class TestCudamiLocalesClient extends CudamiLocalesClient {

    public TestCudamiLocalesClient(String serverUrl, ObjectMapper objectMapper) {
      super(null, serverUrl, objectMapper);
    }

    @Override
    public Locale getDefaultLanguage() throws HttpException {
      return Locale.ENGLISH;
    }
  }
}
