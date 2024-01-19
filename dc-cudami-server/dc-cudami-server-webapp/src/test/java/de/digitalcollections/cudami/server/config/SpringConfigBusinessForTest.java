package de.digitalcollections.cudami.server.config;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierTypeService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.FileResourceMetadataService;
import java.util.Locale;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class SpringConfigBusinessForTest {

  @MockBean public DigitalObjectService digitalObjectService;

  @MockBean(name = "entityService")
  public EntityService entityService;

  @MockBean(name = "fileResourceMetadataService")
  public FileResourceMetadataService fileResourceMetadataService;

  @MockBean(name = "identifiableService")
  public IdentifiableService identifiableService;

  @MockBean public IdentifierTypeService identifierTypeService;

  @Bean
  @Primary
  public LocaleService localeService() {
    final LocaleService dummy = Mockito.mock(LocaleService.class);
    Locale defaultLocale = Locale.ENGLISH;

    Mockito.when(dummy.getDefaultLocale()).thenReturn(defaultLocale);
    return dummy;
  }
}
