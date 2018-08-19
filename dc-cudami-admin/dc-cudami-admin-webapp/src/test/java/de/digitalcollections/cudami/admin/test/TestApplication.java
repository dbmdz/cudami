package de.digitalcollections.cudami.admin.test;

import de.digitalcollections.cudami.admin.config.SpringConfigSecurity;
import de.digitalcollections.cudami.admin.config.SpringConfigWeb;
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
