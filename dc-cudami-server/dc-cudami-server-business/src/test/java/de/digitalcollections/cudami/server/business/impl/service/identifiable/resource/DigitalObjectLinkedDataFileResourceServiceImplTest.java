package de.digitalcollections.cudami.server.business.impl.service.identifiable.resource;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.DigitalObjectLinkedDataFileResourceRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.DigitalObjectLinkedDataFileResourceService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.LinkedDataFileResourceService;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The DigitalObjectLinkedDataFileResourceService")
class DigitalObjectLinkedDataFileResourceServiceImplTest {

  private DigitalObjectLinkedDataFileResourceRepository repo;
  private DigitalObjectLinkedDataFileResourceService service;

  private LinkedDataFileResourceService linkedDataFileResourceService;

  @BeforeEach
  public void beforeEach() throws CudamiServiceException {
    linkedDataFileResourceService = mock(LinkedDataFileResourceService.class);
    repo = mock(DigitalObjectLinkedDataFileResourceRepository.class);
    service =
        new DigitalObjectLinkedDataFileResourceServiceImpl(repo, linkedDataFileResourceService);
  }

  @DisplayName("can delete resource and relation")
  @Test
  public void deleteResourceAndRelation()
      throws CudamiServiceException, IdentifiableServiceException {
    UUID uuid = UUID.randomUUID();
    DigitalObject digitalObject = DigitalObject.builder().uuid(uuid).label("Label").build();
    LinkedDataFileResource linkedDataFileResource = LinkedDataFileResource.builder().build();

    digitalObject.setLinkedDataResources(List.of(linkedDataFileResource));

    when(repo.getLinkedDataFileResources(eq(uuid))).thenReturn(List.of(linkedDataFileResource));

    service.deleteLinkedDataFileResources(uuid);

    verify(repo, times(1)).delete(linkedDataFileResource.getUuid());
    verify(linkedDataFileResourceService, times(1)).delete(linkedDataFileResource.getUuid());
  }
}
