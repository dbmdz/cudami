package io.github.dbmdz.cudami.test;

import io.github.dbmdz.cudami.config.SpringConfigSecurity;
import io.github.dbmdz.cudami.config.SpringConfigWeb;
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
