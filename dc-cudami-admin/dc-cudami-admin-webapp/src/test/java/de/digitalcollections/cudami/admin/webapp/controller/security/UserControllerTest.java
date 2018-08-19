package de.digitalcollections.cudami.admin.webapp.controller.security;

import de.digitalcollections.cudami.admin.test.TestApplication;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * see https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html alternatively:
 * https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html#boot-features-testing-spring-boot-applications-testing-autoconfigured-mvc-tests
 */
// Don’t forget to also add @RunWith(SpringRunner.class) to your test, otherwise the annotations will be ignored:
@ExtendWith(SpringExtension.class)
// annotation which can be used as an alternative to the standard spring-test @ContextConfiguration annotation when you need Spring Boot features:
@SpringBootTest(
        classes = {TestApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
// RANDOM_PORT — Loads an EmbeddedWebApplicationContext and provides a real servlet environment. Embedded servlet containers are started and listening on a random port
// SpringBootTest registers a TestRestTemplate bean for use in web tests that are using a fully running container.
//@ContextConfiguration
public class UserControllerTest {

  @Autowired
  private TestRestTemplate testRestTemplate;

  @Test
  public void resourceRequest() {
    String body = this.testRestTemplate.getForObject("/css/main.css", String.class);
    assertTrue(body.contains("background"));
  }

  @Test
  public void testApiCall() {
    HttpHeaders headers = this.testRestTemplate.headForHeaders("/api/users");
    MediaType contentType = headers.getContentType();
    assertEquals(MediaType.APPLICATION_JSON_UTF8, contentType);
  }

  // see https://docs.spring.io/spring-security/site/docs/5.0.5.RELEASE/reference/htmlsingle/#test-method-withmockuser
  @Test
  @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
  public void testUsersNewRequest() {
    ResponseEntity<Object> responseEntity = this.testRestTemplate.withBasicAuth("admin", "secret").getForEntity("/users/new", Object.class);
    Object object = responseEntity.getBody();
    MediaType contentType = responseEntity.getHeaders().getContentType();
    HttpStatus statusCode = responseEntity.getStatusCode();
  }
}
