package de.digitalcollections.cudami.server.controller.legal;

import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.legal.LicenseService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(LicenseController.class)
@DisplayName("The LicenseControllerTest")
public class LicenseControllerTest extends BaseControllerTest {

  @MockBean private LicenseService licenseService;

  public LicenseControllerTest() {}

  @Test
  public void testCount() throws Exception {
    when(licenseService.count()).thenReturn(23L);
    testGetJsonString("/v5/licenses/count", "23");
  }

  @Test
  public void testDeleteByUrl() {}

  @Test
  public void testDeleteByUuid() {}

  @Test
  public void testDeleteByUuids() {}

  @Test
  public void testFind() {}

  @Test
  public void testGetByUrl() {}

  @Test
  public void testGetByUuid() {}

  @Test
  public void testSave() throws Exception {}

  @Test
  public void testUpdate() throws Exception {}
}
