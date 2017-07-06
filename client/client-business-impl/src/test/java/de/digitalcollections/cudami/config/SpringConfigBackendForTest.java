package de.digitalcollections.cudami.config;

import de.digitalcollections.cudami.client.backend.api.repository.OperationRepository;
import de.digitalcollections.cudami.client.backend.api.repository.RoleRepository;
import de.digitalcollections.cudami.client.backend.api.repository.UserRepository;
import de.digitalcollections.cudami.client.backend.api.repository.WebsiteRepository;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Mock the backend.
 */
@Configuration
public class SpringConfigBackendForTest {

  @Bean
  public OperationRepository operationRepository() {
    return Mockito.mock(OperationRepository.class);
  }

  @Bean
  public RoleRepository roleRepository() {
    return Mockito.mock(RoleRepository.class);
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
