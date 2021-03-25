package de.digitalcollections.cudami.server.controller;

import de.digitalcollections.cudami.server.config.SpringConfigBackendForTest;
import de.digitalcollections.cudami.server.config.SpringConfigBusinessForTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("TEST")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {SpringConfigBusinessForTest.class, SpringConfigBackendForTest.class})
public abstract class BaseControllerTest {

  @Autowired
  protected TestRestTemplate testRestTemplate;

}
