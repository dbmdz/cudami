package io.github.dbmdz.cudami.admin.test;

import io.github.dbmdz.cudami.admin.config.SpringConfigSecurityMonitoring;
import io.github.dbmdz.cudami.admin.config.SpringConfigSecurityWebapp;
import io.github.dbmdz.cudami.admin.config.SpringConfigWeb;
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
