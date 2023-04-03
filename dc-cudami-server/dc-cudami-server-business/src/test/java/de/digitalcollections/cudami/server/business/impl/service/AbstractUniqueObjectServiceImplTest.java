package de.digitalcollections.cudami.server.business.impl.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.UniqueObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.UniqueObject;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public abstract class AbstractUniqueObjectServiceImplTest extends AbstractServiceImplTest {

  protected UniqueObjectRepository repo;
  protected UniqueObjectService service;

  @BeforeEach
  public void beforeEach() throws ServiceException, Exception {
    repo = mock(UniqueObjectRepository.class);
    CudamiConfig cudamiConfig = Mockito.mock(CudamiConfig.class);
    when(cudamiConfig.getOffsetForAlternativePaging()).thenReturn(5000);
  }

  @DisplayName(
      "throws an exception to trigger the rollback, when an exception during deletion happens")
  @Test
  public void throwExceptionWhenDeletionFails() throws ConflictException, ServiceException {
    doThrow(new ServiceException("boo")).when(service).delete(any(Set.class));
    assertThrows(
        ServiceException.class,
        () -> {
          service.delete(Set.of(UUID.randomUUID()));
        });
  }

  @DisplayName("throws an Exception to trigger a rollback on save, when saving in the repo fails")
  @Test
  public void exceptionOnSaveWhenRepoFails() throws RepositoryException {
    doThrow(RepositoryException.class).when(repo).save(any(UniqueObject.class));

    UniqueObject uniqueObject = new UniqueObject() {};

    assertThrows(
        ServiceException.class,
        () -> {
          service.save(uniqueObject);
        });
  }

  @DisplayName(
      "throws an Exception to trigger a rollback on update, when updating in the repo fails")
  @Test
  public void exceptionOnUpdateWhenRepoFails() throws RepositoryException {
    doThrow(NullPointerException.class).when(repo).update(any(UniqueObject.class));

    UniqueObject uniqueObject = new UniqueObject() {};

    assertThrows(
        ServiceException.class,
        () -> {
          service.update(uniqueObject);
        });
  }
}
