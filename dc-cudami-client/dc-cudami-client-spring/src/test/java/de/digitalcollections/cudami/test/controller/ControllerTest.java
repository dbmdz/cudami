package de.digitalcollections.cudami.test.controller;

import de.digitalcollections.cudami.test.environment.Application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * Basic integration tests for service application.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Application.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ControllerTest {

  @Autowired
  private TestRestTemplate testRestTemplate;

  @Test
  public void shouldReturn200WhenSendingRequestToRoot() throws Exception {
    @SuppressWarnings("rawtypes")
    ResponseEntity<String> entity = this.testRestTemplate.getForEntity("/", String.class);

    then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
    then(entity.getBody()).contains("src=\"http://www.test.de\"", "width=\"98%\"", "height=\"auto\"");
  }

}
