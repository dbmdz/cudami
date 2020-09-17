package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CorporationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("The corporation controller")
class CorporationControllerTest {

  private CorporationController corporationController;
  private CorporationService corporationService;

  @BeforeEach
  void setUp() {
    corporationService = mock(CorporationService.class);
    corporationController = new CorporationController(corporationService);
  }

  @ParameterizedTest(
      name =
          "shall accept proper and refuse invalid GND IDs when fetching and retrieving institutions by GND ID")
  @CsvSource(
      value = {"1234-5, true", "12345, true", "1234-X, true", "abcde, false", "abcd-e, false"})
  void gndIdVerification(String gndId, boolean isValue) throws IdentifiableServiceException {
    if (isValue) {
      corporationController.fetchAndSaveByGndId(gndId);
    } else {
      assertThrows(
          IllegalArgumentException.class, () -> corporationController.fetchAndSaveByGndId(gndId));
    }
  }
}
