package de.digitalcollections.cudami.config;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.IdentifierTypeRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.ArticleRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.ContentTreeRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.DigitalObjectRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.WebsiteRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.parts.ContentNodeRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.parts.EntityPartRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.parts.WebpageRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.resource.CudamiFileResourceRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.security.UserRepository;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Mock the backend. */
@Configuration
public class SpringConfigBackendForTest {

  @Bean
  @Qualifier("articleRepositoryImpl")
  public ArticleRepository articleRepositoryImpl() {
    return Mockito.mock(ArticleRepository.class);
  }

  @Bean
  @Qualifier("contentNodeRepositoryImpl")
  public ContentNodeRepository contentNodeRepositoryImpl() {
    return Mockito.mock(ContentNodeRepository.class);
  }

  @Bean
  @Qualifier("contentTreeRepositoryImpl")
  public ContentTreeRepository contentTreeRepositoryImpl() {
    return Mockito.mock(ContentTreeRepository.class);
  }

  @Bean
  @Qualifier("digitalObjectRepositoryImpl")
  public DigitalObjectRepository digitalObjectRepositoryImpl() {
    return Mockito.mock(DigitalObjectRepository.class);
  }

  @Bean
  @Qualifier("entityPartRepositoryImpl")
  public EntityPartRepository entityPartRepositoryImpl() {
    return Mockito.mock(EntityPartRepository.class);
  }

  @Bean
  @Qualifier("entityRepositoryImpl")
  public EntityRepository entityRepositoryImpl() {
    return Mockito.mock(EntityRepository.class);
  }

  @Bean
  @Qualifier("identifiableRepositoryImpl")
  public IdentifiableRepository identifiableRepositoryImpl() {
    return Mockito.mock(IdentifiableRepository.class);
  }

  @Bean
  @Qualifier("identifierTypeRepositoryImpl")
  public IdentifierTypeRepository identifierTypeRepositoryImpl() {
    return Mockito.mock(IdentifierTypeRepository.class);
  }

  @Bean
  @Qualifier("resourceRepositoryImpl")
  public CudamiFileResourceRepository resourceRepositoryImpl() {
    return Mockito.mock(CudamiFileResourceRepository.class);
  }

  @Bean
  @Qualifier("userRepositoryImpl")
  public UserRepository userRepositoryImpl() {
    return Mockito.mock(UserRepository.class);
  }

  @Bean
  @Qualifier("webpageRepositoryImpl")
  public WebpageRepository webpageRepositoryImpl() {
    return Mockito.mock(WebpageRepository.class);
  }

  @Bean
  @Qualifier("websiteRepositoryImpl")
  public WebsiteRepository websiteRepositoryImpl() {
    return Mockito.mock(WebsiteRepository.class);
  }
}
