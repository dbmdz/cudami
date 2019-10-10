package de.digitalcollections.cudami.template.website.springboot;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.web.server.LocalManagementPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/** Basic integration tests for service application. */
@ExtendWith(SpringExtension.class)
@SpringBootTest(
    classes = {Application.class, TestConfig.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"management.server.port=0"})
public class ApplicationTest {

  @LocalServerPort private int port;

  @LocalManagementPort private int mgt;

  @Autowired private TestRestTemplate testRestTemplate;

  @Test
  public void shouldReturn200WhenSendingRequestToRoot() throws Exception {
    @SuppressWarnings("rawtypes")
    ResponseEntity<String> entity =
        this.testRestTemplate.getForEntity("http://localhost:" + this.port + "/", String.class);

    then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void shouldReturn200WhenSendingRequestToManagementEndpoint() throws Exception {
    @SuppressWarnings("rawtypes")
    ResponseEntity<Map> entity =
        this.testRestTemplate.getForEntity(
            "http://localhost:" + this.mgt + "/monitoring/info", Map.class);

    then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
