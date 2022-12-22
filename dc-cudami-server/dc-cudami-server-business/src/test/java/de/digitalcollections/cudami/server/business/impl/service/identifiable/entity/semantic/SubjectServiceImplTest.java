package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.semantic;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.semantic.SubjectRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The SubjectService")
class SubjectServiceImplTest {

  private SubjectServiceImpl subjectService;
  private SubjectRepository subjectRepository;

  @BeforeEach
  public void beforeEach() {
    subjectRepository = mock(SubjectRepository.class);
    subjectService = new SubjectServiceImpl(subjectRepository);
  }

  @DisplayName(
      "throws a ServiceException at getByTypeAndIdentifier when an exception happens in the repository")
  @Test
  public void testCudamiServiceExceptionAtGetByTypeAndIdentifier() {
    when(subjectRepository.getByTypeAndIdentifier(any(), any(), any()))
        .thenThrow(new NullPointerException("boo"));

    assertThrows(
        ServiceException.class,
        () -> subjectService.getByTypeAndIdentifier("type", "namespace", "id"));
  }
}
