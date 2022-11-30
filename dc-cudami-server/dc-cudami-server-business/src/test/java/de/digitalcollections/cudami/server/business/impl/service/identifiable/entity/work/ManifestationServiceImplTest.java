package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.work;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.model.config.CudamiConfig.UrlAlias;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.ManifestationRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation.EntityRelationService;
import de.digitalcollections.cudami.server.config.HookProperties;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

@DisplayName("The ManifestationService")
class ManifestationServiceImplTest {

  private ManifestationRepository manifestationRepository;
  private EntityRelationService entityRelationService;
  private IdentifierService identifierService;
  private LocaleService localeService;
  private ManifestationServiceImpl manifestationService;
  private UrlAliasService urlAliasService;
  private CudamiConfig cudamiConfig;
  private HookProperties hookProperties;

  @BeforeEach
  public void beforeEach() {
    manifestationRepository = mock(ManifestationRepository.class);
    entityRelationService = mock(EntityRelationService.class);
    identifierService = mock(IdentifierService.class);
    urlAliasService = mock(UrlAliasService.class);
    hookProperties = mock(HookProperties.class);
    localeService = mock(LocaleService.class);
    cudamiConfig = mock(CudamiConfig.class);
    UrlAlias urlAliasConfig = mock(UrlAlias.class);
    when(cudamiConfig.getUrlAlias()).thenReturn(urlAliasConfig);
    when(urlAliasConfig.getGenerationExcludes()).thenReturn(List.of("MANIFESTATION"));
    manifestationService =
        new ManifestationServiceImpl(
            manifestationRepository,
            identifierService,
            urlAliasService,
            hookProperties,
            localeService,
            entityRelationService,
            cudamiConfig);
  }
}
