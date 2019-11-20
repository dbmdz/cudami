package de.digitalcollections.cudami.server.config;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import java.util.Locale;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class SpringConfigBusinessForTest {

  @Bean
  @Primary
  public LocaleService localeService() {
    final LocaleService dummy = Mockito.mock(LocaleService.class);
    Locale defaultLocale = Locale.ENGLISH;

    Mockito.when(dummy.getDefaultLocale()).thenReturn(defaultLocale);
    return dummy;
  }
}
