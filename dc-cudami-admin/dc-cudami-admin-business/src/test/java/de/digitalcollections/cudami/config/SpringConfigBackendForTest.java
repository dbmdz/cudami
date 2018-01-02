package de.digitalcollections.cudami.config;

import de.digitalcollections.cudami.client.backend.api.repository.LocaleRepository;
import de.digitalcollections.cudami.client.backend.api.repository.entity.ContentTreeRepository;
import de.digitalcollections.cudami.client.backend.api.repository.identifiable.UserRepository;
import de.digitalcollections.cudami.client.backend.api.repository.entity.WebsiteRepository;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Mock the backend.
 */
@Configuration
public class SpringConfigBackendForTest {

  @Bean
  public ContentTreeRepository contentTreeRepository() {
    return Mockito.mock(ContentTreeRepository.class);
  }

  @Bean
  public LocaleRepository localeRepository() {
    return Mockito.mock(LocaleRepository.class);
  }

  @Bean
  public UserRepository userRepository() {
    return Mockito.mock(UserRepository.class);
  }

  @Bean
  public WebsiteRepository websiteRepository() {
    return Mockito.mock(WebsiteRepository.class);
  }
}
