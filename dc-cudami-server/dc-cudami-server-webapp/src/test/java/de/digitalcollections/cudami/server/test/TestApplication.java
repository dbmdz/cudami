package de.digitalcollections.cudami.server.test;

import de.digitalcollections.cudami.server.config.SpringConfigSecurity;
import de.digitalcollections.cudami.server.config.SpringConfigWeb;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({SpringConfigSecurity.class, SpringConfigWeb.class})
public class TestApplication {

  public static void main(String[] args) throws Exception {
    SpringApplication.run(TestApplication.class, args);
  }
}
