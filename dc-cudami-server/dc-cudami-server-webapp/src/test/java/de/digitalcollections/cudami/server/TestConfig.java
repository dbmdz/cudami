package de.digitalcollections.cudami.server;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.UserService;
import de.digitalcollections.cudami.server.business.api.service.WebsiteService;
import java.util.Locale;
import org.flywaydb.core.Flyway;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestConfig {

  @Bean
  @Primary
  public LocaleService localeService() {
    final LocaleService dummy = Mockito.mock(LocaleService.class);
    Locale defaultLocale = Locale.ENGLISH;

    Mockito.when(dummy.getDefault()).thenReturn(defaultLocale);
    return dummy;
  }

  @Bean
  @Primary
  public UserService userService() {
    final UserService dummy = Mockito.mock(UserService.class);
    return dummy;
  }

  @Bean
  @Primary
  public WebsiteService websiteService() {
    final WebsiteService dummy = Mockito.mock(WebsiteService.class);
    return dummy;
  }

  @Bean
  @Primary
  public Flyway flyway() {
    final Flyway dummy = Mockito.mock(Flyway.class);
    return dummy;
  }
}
