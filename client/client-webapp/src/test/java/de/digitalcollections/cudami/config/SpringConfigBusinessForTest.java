package de.digitalcollections.cudami.config;

import de.digitalcollections.cudami.client.business.api.service.UserService;
import de.digitalcollections.cudami.client.business.api.service.WebsiteService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Services context.
 */
@Configuration
public class SpringConfigBusinessForTest {

  @Bean
  public UserService userService() {
    return Mockito.mock(UserService.class);
  }

  @Bean
  public WebsiteService websiteService() {
    return Mockito.mock(WebsiteService.class);
  }
}
