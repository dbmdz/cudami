package de.digitalcollections.cudami.server.controller.legal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.legal.LicenseService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.legal.License;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.text.LocalizedText;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V5LicenseController.class)
@DisplayName("The V5 LicenseControllerTest")
class V5LicenseControllerTest extends BaseControllerTest {

  @MockBean private LicenseService licenseService;

  private License createTestLicenseSaved() throws MalformedURLException {
    final License license =
        new License(
            "InC-NC 1.0",
            new LocalizedText(Locale.ENGLISH, "IN COPYRIGHT - NON-COMMERCIAL USE PERMITTED"),
            new URL("http://rightsstatements.org/vocab/InC-NC/1.0/"));
    license.setCreated(LocalDateTime.parse("2022-02-01T00:00:00.000000"));
    license.setLastModified(LocalDateTime.parse("2022-02-01T00:00:00.000000"));
    license.setUuid(UUID.fromString("2780bee1-eeec-4b50-a95b-bba90793fc6a"));
    return license;
  }

  @Test
  public void testFind() throws MalformedURLException, Exception {
    License license = createTestLicenseSaved();
    PageResponse<License> pageResponse =
        PageResponse.builder()
            .forRequestPage(0)
            .forPageSize(1)
            .withTotalElements(1)
            .forAscendingOrderedField("label", "de")
            .withContent(license)
            .build();
    when(licenseService.find(any(PageRequest.class))).thenReturn(pageResponse);

    testJson("/v5/licenses?pageNumber=0&pageSize=1", "/v5/legal/licenses/find_with_result.json");
  }
}
