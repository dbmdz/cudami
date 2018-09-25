package de.digitalcollections.cudami.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * File uploads: As part of auto-configuring Spring MVC, Spring Boot will create a MultipartConfigElement
 * bean and make itself ready for file uploads.
 */
@SpringBootApplication
public class Application {

  public static void main(String[] args) throws Exception {
    SpringApplication.run(Application.class, args);
  }
}
