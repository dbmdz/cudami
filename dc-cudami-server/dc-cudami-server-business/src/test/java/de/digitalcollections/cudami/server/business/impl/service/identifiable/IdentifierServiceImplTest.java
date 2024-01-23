package de.digitalcollections.cudami.server.business.impl.service.identifiable;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierTypeService;
import de.digitalcollections.cudami.server.business.impl.service.AbstractUniqueObjectServiceImplTest;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.validation.ValidationException;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The Identifier Service")
public class IdentifierServiceImplTest
    extends AbstractUniqueObjectServiceImplTest<
        Identifier, IdentifierRepository, IdentifierService> {

  private IdentifierTypeService identifierTypeService;

  @BeforeEach
  public void beforeEach() throws ServiceException {
    repo = mock(IdentifierRepository.class);
    identifierTypeService = mock(IdentifierTypeService.class);
    service = new IdentifierServiceImpl(repo, identifierTypeService);
  }

  @DisplayName("Validation succeeds if all conditions are met")
  @Test
  public void validationSuccess() throws ServiceException {
    when(identifierTypeService.getIdentifierTypeCache()).thenReturn(Map.of("namespace", "id"));

    Set<Identifier> identifiers =
        Set.of(Identifier.builder().namespace("namespace").id("id").build());

    assertThatCode(() -> service.validate(identifiers)).doesNotThrowAnyException();
  }

  @DisplayName("Validation succeeds if all conditions are met after an update of the cache")
  @Test
  public void validationSuccessAfterUpdate() throws ServiceException {
    when(identifierTypeService.getIdentifierTypeCache()).thenReturn(Map.of("namespace1", "id1"));
    when(identifierTypeService.updateIdentifierTypeCache())
        .thenReturn(Map.of("namespace1", "id1", "namespace2", "id2"));

    Set<Identifier> identifiers =
        Set.of(Identifier.builder().namespace("namespace2").id("id2").build());

    assertThatCode(() -> service.validate(identifiers)).doesNotThrowAnyException();
  }

  @DisplayName("Validation fails if the namespace is not found")
  @Test
  public void validationFailureNamespace() throws ServiceException {
    when(identifierTypeService.getIdentifierTypeCache()).thenReturn(Map.of("namespace", "id"));
    when(identifierTypeService.updateIdentifierTypeCache()).thenReturn(Map.of("namespace", "id"));

    Set<Identifier> identifiers =
        Set.of(Identifier.builder().namespace("namespace2").id("id2").build());

    assertThatExceptionOfType(ValidationException.class)
        .isThrownBy(() -> service.validate(identifiers))
        .withMessageContaining("namespacesNotFound=[namespace2]");
  }

  @DisplayName("Validation fails if the id does not match the pattern")
  @Test
  public void validationFailurePattern() throws ServiceException {
    when(identifierTypeService.getIdentifierTypeCache()).thenReturn(Map.of("namespace", "id"));
    when(identifierTypeService.updateIdentifierTypeCache()).thenReturn(Map.of("namespace", "id"));

    Set<Identifier> identifiers =
        Set.of(Identifier.builder().namespace("namespace").id("id2").build());

    assertThatExceptionOfType(ValidationException.class)
        .isThrownBy(() -> service.validate(identifiers))
        .withMessageContaining("idsNotMatchingPattern=[namespace:id2]");
  }

  @DisplayName("Validation fails, if the id is null")
  @Test
  public void validationFailureNullId() throws ServiceException {
    when(identifierTypeService.getIdentifierTypeCache()).thenReturn(Map.of("namespace", "id"));
    when(identifierTypeService.updateIdentifierTypeCache()).thenReturn(Map.of("namespace", "id"));

    Set<Identifier> identifiers =
        Set.of(Identifier.builder().namespace("namespace").id(null).build());

    assertThatExceptionOfType(ValidationException.class)
        .isThrownBy(() -> service.validate(identifiers))
        .withMessageContaining("idsNotMatchingPattern=[namespace:null]");
  }
}
