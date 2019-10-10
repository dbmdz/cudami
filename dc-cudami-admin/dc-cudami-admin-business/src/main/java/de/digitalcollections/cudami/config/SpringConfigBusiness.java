package de.digitalcollections.cudami.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/** Services context. */
@Configuration
@ComponentScan(
    basePackages = {
      "de.digitalcollections.cudami.admin.business.impl.service",
      "de.digitalcollections.cudami.admin.business.impl.validator"
    })
public class SpringConfigBusiness {}
