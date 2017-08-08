package de.digitalcollections.cudami.client.config;

import de.digitalcollections.cudami.client.business.api.service.UserService;
import de.digitalcollections.cudami.client.business.api.service.WebsiteService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Services context.
 */
@Configuration
public class SpringConfigBusinessForTest {

  @Primary
  @Bean
  public UserService userService() {
    return Mockito.mock(UserService.class);
  }

  @Primary
  @Bean
  public WebsiteService websiteService() {
    return Mockito.mock(WebsiteService.class);
  }
}
