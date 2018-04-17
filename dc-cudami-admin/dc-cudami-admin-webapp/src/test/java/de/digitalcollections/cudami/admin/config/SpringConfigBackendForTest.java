package de.digitalcollections.cudami.admin.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

/**
 * Backend mocks.
 */
@Configuration
public class SpringConfigBackendForTest {

  @Primary
  @Bean
  public PersistentTokenRepository persistentTokenRepository() {
    return Mockito.mock(PersistentTokenRepository.class);
  }
}
