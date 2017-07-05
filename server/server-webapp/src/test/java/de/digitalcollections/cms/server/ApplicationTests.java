package de.digitalcollections.cms.server;

import de.digitalcollections.cms.server.Application;
import java.util.Map;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Basic integration tests for service application.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"management.port=0"})
public class ApplicationTests {

  @LocalServerPort
  private int port;

  @Value("${local.management.port}")
  private int mgt;

  @Autowired
  private TestRestTemplate testRestTemplate;

  @Test
  public void shouldReturn200WhenSendingRequestToController() throws Exception {
    @SuppressWarnings("rawtypes")
    ResponseEntity<Map> entity = this.testRestTemplate.getForEntity(
            "http://localhost:" + this.port + "/hello", Map.class);

    then(entity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void shouldReturn200WhenSendingRequestToManagementEndpoint() throws Exception {
    @SuppressWarnings("rawtypes")
    ResponseEntity<Map> entity = this.testRestTemplate.getForEntity(
            "http://localhost:" + this.mgt + "/info", Map.class);

    then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

}
