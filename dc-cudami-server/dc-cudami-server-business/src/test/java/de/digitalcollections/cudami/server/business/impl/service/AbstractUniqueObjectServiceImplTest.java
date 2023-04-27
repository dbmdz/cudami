package de.digitalcollections.cudami.server.business.impl.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.UniqueObjectRepository;
import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.UniqueObject;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

public abstract class AbstractUniqueObjectServiceImplTest<
        U extends UniqueObject,
        R extends UniqueObjectRepository<U>,
        S extends UniqueObjectService<U>>
    extends AbstractServiceImplTest {

  protected R repo;
  protected S service;

  @BeforeEach
  public void beforeEach() throws ServiceException, Exception {
    super.beforeEach();
    repo = (R) mock(UniqueObjectRepository.class);
    CudamiConfig cudamiConfig = Mockito.mock(CudamiConfig.class);
    when(cudamiConfig.getOffsetForAlternativePaging()).thenReturn(5000);
  }

  // FIXME: move to service tests where applicable
  //  @DisplayName(
  //      "throws an exception to trigger the rollback, when an exception during deletion happens")
  //  @Test
  //  public void throwExceptionWhenDeletionFails() throws ConflictException, ServiceException {
  //    doThrow(new ServiceException("boo")).when(service).delete(any(Set.class));
  //    assertThrows(
  //        ServiceException.class,
  //        () -> {
  //          service.delete((Set<U>) Set.of(createUniqueObject()));
  //        });
  //  }
  //
  //  @DisplayName("throws an Exception to trigger a rollback on save, when saving in the repo
  // fails")
  //  @Test
  //  public void exceptionOnSaveWhenRepoFails() throws RepositoryException {
  //    doThrow(RepositoryException.class).when(repo).save((U) any(UniqueObject.class));
  //
  //    UniqueObject uniqueObject = new UniqueObject() {};
  //
  //    assertThrows(
  //        ServiceException.class,
  //        () -> {
  //          service.save((U) uniqueObject);
  //        });
  //  }
  //
  //  @DisplayName(
  //      "throws an Exception to trigger a rollback on update, when updating in the repo fails")
  //  @Test
  //  public void exceptionOnUpdateWhenRepoFails() throws RepositoryException {
  //    doThrow(NullPointerException.class).when(repo).update((U) any(UniqueObject.class));
  //
  //    UniqueObject uniqueObject = new UniqueObject() {};
  //
  //    assertThrows(
  //        ServiceException.class,
  //        () -> {
  //          service.update((U) uniqueObject);
  //        });
  //  }
}
