package de.digitalcollections.cudami.client.config;

import de.digitalcollections.cudami.client.rest.Cudami;
import de.digitalcollections.cudami.client.rest.Environment;
import de.digitalcollections.cudami.client.rest.api.CudamiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(
    basePackages = "de.digitalcollections.cudami.client"
)
public class SpringConfigRest {

  @Bean
  public Cudami cudami(@Value("${cudami.environment}") String environment) {
    return new Cudami(Environment.fromString(environment));
  }

  @Bean
  public CudamiClient cudamiClient(Cudami cudami) {
    return cudami.cudamiClient();
  }

}
