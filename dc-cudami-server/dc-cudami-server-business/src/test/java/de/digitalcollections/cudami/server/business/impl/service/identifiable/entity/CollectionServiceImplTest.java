package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.CollectionRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The CollectionService")
class CollectionServiceImplTest {

  private CollectionServiceImpl collectionService;
  private CollectionRepository collectionRepository;
  private IdentifierService identifierService;
  private UrlAliasService urlAliasService;
  private HookProperties hookProperties;
  private LocaleService localeService;
  private CudamiConfig cudamiConfig;

  @BeforeEach
  public void beforeEach() {
    collectionRepository = mock(CollectionRepository.class);
    identifierService = mock(IdentifierService.class);
    urlAliasService = mock(UrlAliasService.class);
    hookProperties = mock(HookProperties.class);
    localeService = mock(LocaleService.class);
    cudamiConfig = mock(CudamiConfig.class);
    collectionService =
        new CollectionServiceImpl(
            collectionRepository,
            identifierService,
            urlAliasService,
            hookProperties,
            localeService,
            cudamiConfig);
  }

  @DisplayName("throws an exception, when a collection with children shall be deleted")
  @Test
  public void execeptionOnDeletionOfCollectionWithChildren() {
    PageResponse<Collection> pageResponse = mock(PageResponse.class);
    when(pageResponse.getTotalElements()).thenReturn(1l);
    NodeRepository<Collection> nodeRepository = mock(NodeRepository.class);
    when(nodeRepository.findChildren(any(UUID.class), any(PageRequest.class)))
        .thenReturn(pageResponse);
    collectionService.setNodeRepository(nodeRepository);

    assertThrows(ConflictException.class, () -> collectionService.delete(UUID.randomUUID()));
  }

  @DisplayName(
      "throws an exception, when a collection with attached DigitalObjects shall be deleted")
  @Test
  public void execeptionOnDeletionOfFilledCollection() {
    PageResponse<DigitalObject> pageResponse = mock(PageResponse.class);
    when(pageResponse.getTotalElements()).thenReturn(1l);
    when(collectionRepository.findDigitalObjects(any(UUID.class), any(PageRequest.class)))
        .thenReturn(pageResponse);

    assertThrows(ConflictException.class, () -> collectionService.delete(UUID.randomUUID()));
  }
}
