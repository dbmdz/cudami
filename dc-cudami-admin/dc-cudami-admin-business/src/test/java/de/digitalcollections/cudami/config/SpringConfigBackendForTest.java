package de.digitalcollections.cudami.config;

import de.digitalcollections.cudami.admin.backend.api.repository.LocaleRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.ContentTreeRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.WebsiteRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.resource.WebpageRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.security.UserRepository;
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
  public WebpageRepository webpageRepository() {
    return Mockito.mock(WebpageRepository.class);
  }

  @Bean
  public WebsiteRepository websiteRepository() {
    return Mockito.mock(WebsiteRepository.class);
  }
}
