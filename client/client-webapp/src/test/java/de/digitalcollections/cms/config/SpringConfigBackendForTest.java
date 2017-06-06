package de.digitalcollections.cms.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

/**
 * Backend mocks.
 */
@Configuration
public class SpringConfigBackendForTest {

  @Bean
  public PersistentTokenRepository persistentTokenRepository() {
    return Mockito.mock(PersistentTokenRepository.class);
  }
}
