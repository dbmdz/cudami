package de.digitalcollections.cudami.server.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan(
    basePackages = {
      "de.digitalcollections.cudami.server.business.impl.service",
      "de.digitalcollections.cudami.server.business.impl.validator"
    })
@EnableTransactionManagement
public class SpringConfigBusiness {}
