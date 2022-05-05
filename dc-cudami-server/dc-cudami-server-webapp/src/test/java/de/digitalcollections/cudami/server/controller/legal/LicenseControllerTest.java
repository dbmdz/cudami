package de.digitalcollections.cudami.server.controller.legal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.legal.LicenseService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.legal.License;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.text.LocalizedText;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(LicenseController.class)
@DisplayName("The LicenseControllerTest")
public class LicenseControllerTest extends BaseControllerTest {

  @MockBean private LicenseService licenseService;

  public LicenseControllerTest() {}

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

  private License createTestLicenseUnsaved() throws MalformedURLException {
    final License license =
        new License(
            "InC-NC 1.0",
            new LocalizedText(Locale.ENGLISH, "IN COPYRIGHT - NON-COMMERCIAL USE PERMITTED"),
            new URL("http://rightsstatements.org/vocab/InC-NC/1.0/"));
    return license;
  }

  @Test
  public void testCount() throws Exception {
    when(licenseService.count()).thenReturn(23L);
    testGetJsonString("/v5/licenses/count", "23");
  }

  @Test
  public void testDeleteByUrl() throws Exception {
    testDeleteSuccessful(
        "/v5/licenses?url="
            + URLEncoder.encode(
                "http://rightsstatements.org/vocab/InC-NC/1.0/", StandardCharsets.UTF_8));
  }

  @Test
  public void testDeleteByUuid() throws Exception {
    testDeleteSuccessful("/v5/licenses/599a120c-2dd5-11e8-b467-0ed5f89f718b");
  }

  @Test
  public void testDeleteByUuids() throws Exception {
    String jsonBody =
        "["
            + "\"599a120c-2dd5-11e8-b467-0ed5f89f718a\", "
            + "\"599a120c-2dd5-11e8-b467-0ed5f89f718b\", "
            + "\"599a120c-2dd5-11e8-b467-0ed5f89f718c\", "
            + "\"599a120c-2dd5-11e8-b467-0ed5f89f718d\""
            + "]";
    testDeleteJsonSuccessful("/v5/licenses", jsonBody);
  }

  @Test
  public void testFind() throws MalformedURLException, Exception {
    License license = createTestLicenseSaved();
    PageResponse<License> pageResponse =
        PageResponse.builder()
            .forRequestPage(0)
            .forPageSize(1)
            .withTotalElements(1)
            .withContent(license)
            .build();
    when(licenseService.find(any(PageRequest.class))).thenReturn(pageResponse);

    testJson("/v5/licenses?pageNumber=0&pageSize=1", "/v5/legal/licenses/find_with_result.json");
  }

  @Test
  public void testGetByUrl() throws UnsupportedEncodingException, Exception {
    License license = createTestLicenseSaved();
    when(licenseService.getByUrl(any(URL.class))).thenReturn(license);

    testJson(
        "/v5/licenses?url="
            + URLEncoder.encode(
                "http://rightsstatements.org/vocab/InC-NC/1.0/", StandardCharsets.UTF_8),
        "/v5/legal/licenses/license_response.json");
  }

  @Test
  public void testGetByUuid() throws Exception {
    License license = createTestLicenseSaved();
    when(licenseService.getByUuid(any(UUID.class))).thenReturn(license);

    testJson(
        "/v5/licenses/2780bee1-eeec-4b50-a95b-bba90793fc6a",
        "/v5/legal/licenses/license_response.json");
  }

  @Test
  public void testSave() throws Exception {
    String jsonBody =
        "{\n"
            + "  \"acronym\": \"InC-NC 1.0\",\n"
            + "  \"label\": {\n"
            + "    \"en\": \"IN COPYRIGHT - NON-COMMERCIAL USE PERMITTED\"\n"
            + "  },\n"
            + "  \"url\": \"http://rightsstatements.org/vocab/InC-NC/1.0/\"\n"
            + "}";
    License savedLicense = createTestLicenseSaved();
    when(licenseService.save(any(License.class))).thenReturn(savedLicense);

    testPostJson("/v5/licenses", jsonBody, "/v5/legal/licenses/license_response.json");
  }

  @Test
  public void testUpdate() throws Exception {
    String jsonBody =
        "{\n"
            + "  \"acronym\": \"InC-NC 1.0\",\n"
            + "  \"label\": {\n"
            + "    \"en\": \"IN COPYRIGHT - NON-COMMERCIAL USE PERMITTED\"\n"
            + "  },\n"
            + "  \"url\": \"http://rightsstatements.org/vocab/InC-NC/1.0/\",\n"
            + "  \"uuid\": \"2780bee1-eeec-4b50-a95b-bba90793fc6a\"\n"
            + "}";
    License savedLicense = createTestLicenseSaved();
    when(licenseService.update(any(License.class))).thenReturn(savedLicense);

    testPutJson(
        "/v5/licenses/2780bee1-eeec-4b50-a95b-bba90793fc6a",
        jsonBody,
        "/v5/legal/licenses/license_response.json");
  }
}
