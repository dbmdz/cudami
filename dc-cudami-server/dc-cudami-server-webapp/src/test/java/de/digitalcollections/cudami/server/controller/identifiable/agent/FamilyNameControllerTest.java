package de.digitalcollections.cudami.server.controller.identifiable.agent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.agent.FamilyNameService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.agent.FamilyName;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(FamilyNameController.class)
@DisplayName("The FamilyNameController")
class FamilyNameControllerTest extends BaseControllerTest {

  @MockBean private FamilyNameService familyNameService;

  @DisplayName("shall return a paged list of family names")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/familynames?pageSize=1&pageNumber=0"})
  void testFind(String path) throws Exception {
    PageResponse<FamilyName> expected =
        (PageResponse<FamilyName>)
            PageResponse.builder()
                .forRequestPage(0)
                .forPageSize(1)
                .withTotalElements(0)
                .forAscendingOrderedField("label", "de")
                .forAscendingOrderedField("label")
                .forAscendingOrderedField("uuid")
                .build();

    when(familyNameService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v6/familynames/find_with_empty_result.json");
  }

  @DisplayName("can retrieve by identifier with plaintext id")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v6/familynames/identifier/foo:bar",
        "/v5/familynames/identifier/foo:bar",
        "/v6/familynames/identifier/foo:bar.json",
        "/v5/familynames/identifier/foo:bar.json"
      })
  void testGetByIdentifierWithPlaintextId(String path) throws Exception {
    FamilyName expected = new FamilyName();

    when(familyNameService.getByIdentifier(
            eq(Identifier.builder().namespace("foo").id("bar").build())))
        .thenReturn(expected);

    testHttpGet(path);

    verify(familyNameService, times(1))
        .getByIdentifier(eq(Identifier.builder().namespace("foo").id("bar").build()));
  }

  @DisplayName("can retrieve by identifier with base 64 encoded data")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/familynames/identifier/", "/v5/familynames/identifier/"})
  void testGetByIdentifierWithBase64EncodedData(String basePath) throws Exception {
    FamilyName expected = new FamilyName();

    when(familyNameService.getByIdentifier(
            eq(Identifier.builder().namespace("foo").id("bar/bla").build())))
        .thenReturn(expected);

    testHttpGet(
        basePath
            + Base64.getEncoder().encodeToString("foo:bar/bla".getBytes(StandardCharsets.UTF_8)));

    verify(familyNameService, times(1))
        .getByIdentifier(eq(Identifier.builder().namespace("foo").id("bar/bla").build()));
  }
}
