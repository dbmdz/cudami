package de.digitalcollections.cudami.server;

import de.digitalcollections.cudami.server.config.SpringConfigBackendForTest;
import de.digitalcollections.cudami.server.config.SpringConfigBusinessForTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("TEST")
@SpringBootTest(classes = {SpringConfigBusinessForTest.class, SpringConfigBackendForTest.class})
public class ApplicationTest {

  /* This method tests, if the application comes up, not more */
  @Test
  public void contextLoads() throws Exception {}
}
