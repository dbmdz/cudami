package de.digitalcollections.cudami.admin.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(
    basePackages = {
      "de.digitalcollections.commons.springboot.actuator",
      "de.digitalcollections.commons.springboot.contributor",
      "de.digitalcollections.commons.springboot.monitoring",
      "de.digitalcollections.cudami.config"
    })
public class SpringConfig {}
