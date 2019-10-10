package de.digitalcollections.cudami.server.test.config;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ArticleService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ContentTreeService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.WebsiteService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.parts.ContentNodeService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.parts.WebpageService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.FileResourceService;
import de.digitalcollections.cudami.server.business.api.service.security.UserService;
import java.util.Locale;
import org.flywaydb.core.Flyway;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SpringConfigBusinessForTest {

  @Primary
  @Bean
  public ArticleService articleService() {
    return Mockito.mock(ArticleService.class);
  }

  @Primary
  @Bean
  public ContentNodeService contentNodeService() {
    return Mockito.mock(ContentNodeService.class);
  }

  @Primary
  @Bean
  public ContentTreeService contentTreeService() {
    return Mockito.mock(ContentTreeService.class);
  }

  @Primary
  @Bean
  public FileResourceService cudamiFileResourceService() {
    return Mockito.mock(FileResourceService.class);
  }

  @Primary
  @Bean
  public EntityService entityService() {
    return Mockito.mock(EntityService.class);
  }

  @Bean
  @Primary
  public Flyway flyway() {
    return Mockito.mock(Flyway.class);
  }

  @Primary
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

  @Bean
  @Primary
  public UserService userService() {
    return Mockito.mock(UserService.class);
  }

  @Primary
  @Bean
  public WebpageService webpageService() {
    return Mockito.mock(WebpageService.class);
  }

  @Primary
  @Bean
  public WebsiteService websiteService() {
    return Mockito.mock(WebsiteService.class);
  }
}
