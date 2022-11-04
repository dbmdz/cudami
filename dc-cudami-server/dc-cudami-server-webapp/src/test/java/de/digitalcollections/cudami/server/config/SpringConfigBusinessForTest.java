package de.digitalcollections.cudami.server.config;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.FileResourceMetadataService;
import java.util.Locale;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class SpringConfigBusinessForTest {

  @Bean
  public DigitalObjectService digitalObjectService() {
    return Mockito.mock(DigitalObjectService.class);
  }

  @Bean
  public EntityService entityService() {
    return Mockito.mock(EntityService.class);
  }

  @Bean
  public FileResourceMetadataService fileResourceMetadataService() {
    return Mockito.mock(FileResourceMetadataService.class);
  }

  @Bean
  public IdentifiableService identifiableService() {
    return Mockito.mock(IdentifiableService.class);
  }

  @Bean
  @Primary
  public LocaleService localeService() {
    final LocaleService dummy = Mockito.mock(LocaleService.class);
    Locale defaultLocale = Locale.ENGLISH;

    Mockito.when(dummy.getDefaultLocale()).thenReturn(defaultLocale);
    return dummy;
  }
}
