package de.digitalcollections.cudami.config;

import de.digitalcollections.cudami.admin.backend.api.repository.LocaleRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.ArticleRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.ContentTreeRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.WebsiteRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.parts.ContentNodeRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.parts.WebpageRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.resource.CudamiFileResourceRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.security.UserRepository;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Mock the backend.
 */
@Configuration
public class SpringConfigBackendForTest {

  @Bean
  public ArticleRepository articleRepository() {
    return Mockito.mock(ArticleRepository.class);
  }

  @Bean
  @Qualifier("identifiableRepositoryImpl")
  public IdentifiableRepository identifiableRepositoryImpl() {
    return Mockito.mock(IdentifiableRepository.class);
  }

  @Bean
  @Qualifier("resourceRepositoryImpl")
  public CudamiFileResourceRepository resourceRepositoryImpl() {
    return Mockito.mock(CudamiFileResourceRepository.class);
  }

  @Bean
  @Qualifier("entityRepositoryImpl")
  public EntityRepository entityRepositoryImpl() {
    return Mockito.mock(EntityRepository.class);
  }

  @Bean
  public ContentNodeRepository contentNodeRepository() {
    return Mockito.mock(ContentNodeRepository.class);
  }

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
