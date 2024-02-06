package de.digitalcollections.cudami.server.controller.identifiable.entity.agent;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.agent.CorporateBodyService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.validation.ValidationException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(CorporateBodyController.class)
@DisplayName("The CorporateBodyController")
class CorporateBodyControllerTest extends BaseControllerTest {

  @MockBean private CorporateBodyService corporateBodyService;

  private CorporateBodyController corporateBodyController;

  @BeforeEach
  public void beforeEach() {
    corporateBodyController = new CorporateBodyController(corporateBodyService);
  }

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
  void gndIdVerification(String gndId, boolean isValue)
      throws ServiceException, ValidationException {
    if (isValue) {
      corporateBodyController.fetchAndSaveByGndId(gndId);
    } else {
      assertThrows(
          IllegalArgumentException.class, () -> corporateBodyController.fetchAndSaveByGndId(gndId));
    }
  }

  @DisplayName("can retrieve by identifier with plaintext id")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v6/corporatebodies/identifier/foo:bar",
        "/v5/corporatebodies/identifier/foo:bar",
        "/v3/corporatebodies/identifier/foo:bar",
        "/latest/corporatebodies/identifier/foo:bar",
        "/v6/corporatebodies/identifier/foo:bar.json",
        "/v5/corporatebodies/identifier/foo:bar.json",
        "/v3/corporatebodies/identifier/foo:bar.json",
        "/latest/corporatebodies/identifier/foo:bar.json"
      })
  void testGetByIdentifierWithPlaintextId(String path) throws Exception {
    CorporateBody expected = CorporateBody.builder().build();

    when(corporateBodyService.getByIdentifier(
            eq(Identifier.builder().namespace("foo").id("bar").build())))
        .thenReturn(expected);

    testHttpGet(path);

    verify(corporateBodyService, times(1))
        .getByIdentifier(eq(Identifier.builder().namespace("foo").id("bar").build()));
  }

  @DisplayName("can retrieve by identifier with base 64 encoded data")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v6/corporatebodies/identifier/",
        "/v5/corporatebodies/identifier/",
        "/v3/corporatebodies/identifier/",
        "/latest/corporatebodies/identifier/"
      })
  void testGetByIdentifierWithBase64EncodedData(String basePath) throws Exception {
    CorporateBody expected = CorporateBody.builder().build();

    when(corporateBodyService.getByIdentifier(
            eq(Identifier.builder().namespace("foo").id("bar/bla").build())))
        .thenReturn(expected);

    testHttpGet(
        basePath
            + Base64.getEncoder().encodeToString("foo:bar/bla".getBytes(StandardCharsets.UTF_8)));

    verify(corporateBodyService, times(1))
        .getByIdentifier(eq(Identifier.builder().namespace("foo").id("bar/bla").build()));
  }
}
