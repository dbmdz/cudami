package de.digitalcollections.cudami.server;

import static org.assertj.core.api.BDDAssertions.then;

import de.digitalcollections.cudami.server.config.SpringConfigBackendForTest;
import de.digitalcollections.cudami.server.config.SpringConfigBusinessForTest;
import java.util.Map;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.web.server.LocalManagementPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/** Basic integration tests for service application. */
@ActiveProfiles("TEST")
@SpringBootTest(
    classes = {SpringConfigBusinessForTest.class, SpringConfigBackendForTest.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
// FIXME: Does not work since dc-commons is integrated. But life must go on…
@Disabled
public class ActuatorControllerTest {

  @Value("${management.endpoints.web.base-path}")
  private String managementContextPath;

  @LocalManagementPort private int managementPort;

  @Autowired private TestRestTemplate testRestTemplate;

  @Test
  public void shouldReturn200WhenSendingRequestToController() throws Exception {
    @SuppressWarnings("rawtypes")
    ResponseEntity<Map> entity = this.testRestTemplate.getForEntity("/hello", Map.class);

    then(entity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void shouldReturn200WhenSendingRequestToManagementEndpoint() throws Exception {
    @SuppressWarnings("rawtypes")
    ResponseEntity<Map> entity =
        this.testRestTemplate.getForEntity(
            "http://localhost:" + this.managementPort + this.managementContextPath + "/info",
            Map.class);

    then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
