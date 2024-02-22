package io.github.dbmdz.cudami.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/*
 * File uploads: As part of auto-configuring Spring MVC, Spring Boot will create a MultipartConfigElement
 * bean and make itself ready for file uploads.
 */
@ConfigurationPropertiesScan
@SpringBootApplication
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
