package io.github.dbmdz.cudami.webapp.controller.security;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.dbmdz.cudami.test.TestApplication;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * see https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html
 * alternatively:
 * https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html#boot-features-testing-spring-boot-applications-testing-autoconfigured-mvc-tests
 */
@SpringBootTest(
    classes = {TestApplication.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// RANDOM_PORT — Loads an EmbeddedWebApplicationContext and provides a real servlet environment.
// Embedded servlet containers are started and listening on a random port
// SpringBootTest registers a TestRestTemplate bean for use in web tests that are using a fully
// running container.
@ConfigurationPropertiesScan(
    basePackages = {"io.github.dbmdz.cudami.config", "io.github.dbmdz.cudami.test.config"})
public class UserControllerTest {

  @Autowired private TestRestTemplate testRestTemplate;

  @Test
  public void resourceRequest() {
    String body = this.testRestTemplate.getForObject("/css/main.css", String.class);
    assertThat(body.contains("font-family")).isTrue();
  }

  // see
  // https://docs.spring.io/spring-security/site/docs/5.0.5.RELEASE/reference/htmlsingle/#test-method-withmockuser
  @Test
  @WithMockUser(
      username = "admin",
      roles = {"USER", "ADMIN"})
  public void testUsersNewRequest() {
    ResponseEntity<Object> responseEntity =
        this.testRestTemplate
            .withBasicAuth("admin", "secret")
            .getForEntity("/users/new", Object.class);
    assertThat(responseEntity.getStatusCode())
        .is(
            new Condition<>(
                status -> status.is3xxRedirection(),
                "No support for Basic Auth but redirection to login page failed."));
    assertThat(responseEntity.getHeaders().getLocation()).isNotNull().hasPath("/login");
  }
}
