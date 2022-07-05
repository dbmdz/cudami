package de.digitalcollections.cudami.server.business.impl.service.identifiable;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.model.identifiable.Identifier;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The Identifier Service")
public class IdentifierServiceImplTest {

  private IdentifierService service;
  private IdentifierRepository repo;
  private IdentifierTypeServiceImpl identifierTypeService;

  @BeforeEach
  public void beforeEach() throws CudamiServiceException {
    repo = mock(IdentifierRepository.class);
    identifierTypeService = mock(IdentifierTypeServiceImpl.class);
    service = new IdentifierServiceImpl(repo, identifierTypeService);
  }

  @DisplayName("validation succeeds if all conditions are met")
  @Test
  public void validationSuccess() {
    when(identifierTypeService.getIdentifierTypeCache()).thenReturn(Map.of("namespace", "id"));

    Set<Identifier> identifiers = Set.of(new Identifier("id", "namespace"));

    assertThatCode(() -> service.validate(identifiers)).doesNotThrowAnyException();
  }

  @DisplayName("validation succeeds if all conditions are met after an update of the cache")
  @Test
  public void validationSuccessAfterUpdate() throws CudamiServiceException {
    when(identifierTypeService.getIdentifierTypeCache()).thenReturn(Map.of("namespace1", "id1"));
    when(identifierTypeService.updateIdentifierTypeCache())
        .thenReturn(Map.of("namespace1", "id1", "namespace2", "id2"));

    Set<Identifier> identifiers = Set.of(new Identifier("id2", "namespace2"));

    assertThatCode(() -> service.validate(identifiers)).doesNotThrowAnyException();
  }

  public void validationFailureNamespace() {}

  public void validationFailurePattern() {}
}
