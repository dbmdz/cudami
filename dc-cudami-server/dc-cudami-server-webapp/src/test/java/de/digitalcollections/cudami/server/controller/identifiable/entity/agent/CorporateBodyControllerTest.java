package de.digitalcollections.cudami.server.controller.identifiable.entity.agent;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.agent.CorporateBodyService;
import de.digitalcollections.cudami.server.config.SpringConfigBackendForTest;
import de.digitalcollections.cudami.server.config.SpringConfigBusinessForTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@DisplayName("The corporate body controller")
@ActiveProfiles("TEST")
@SpringBootTest(classes = {SpringConfigBusinessForTest.class, SpringConfigBackendForTest.class})
class CorporateBodyControllerTest {

  private CorporateBodyController corporateBodyController;
  private CorporateBodyService corporateBodyService;

  @ParameterizedTest(
      name =
          "shall accept proper and refuse invalid GND IDs when fetching and retrieving institutions by GND ID")
  @CsvSource(
      value = {
        "1234-5, true",
        "12345, true",
        "1234-X, true",
        "abcde, false",
        "abcd-e, false",
        "11489308X, true"
      })
  void gndIdVerification(String gndId, boolean isValue) throws IdentifiableServiceException {
    if (isValue) {
      corporateBodyController.fetchAndSaveByGndId(gndId);
    } else {
      assertThrows(
          IllegalArgumentException.class, () -> corporateBodyController.fetchAndSaveByGndId(gndId));
    }
  }

  @BeforeEach
  void setUp() {
    corporateBodyService = mock(CorporateBodyService.class);
    corporateBodyController = new CorporateBodyController(corporateBodyService);
  }
}
