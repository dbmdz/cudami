package de.digitalcollections.cudami.admin.test;

import de.digitalcollections.cudami.admin.config.SpringConfigSecurityMonitoring;
import de.digitalcollections.cudami.admin.config.SpringConfigSecurityWebapp;
import de.digitalcollections.cudami.admin.config.SpringConfigWeb;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({
  SpringConfigSecurityMonitoring.class,
  SpringConfigSecurityWebapp.class,
  SpringConfigWeb.class
})
public class TestApplication {

  public static void main(String[] args) throws Exception {
    SpringApplication.run(TestApplication.class, args);
  }
}
