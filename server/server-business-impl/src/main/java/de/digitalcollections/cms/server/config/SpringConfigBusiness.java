package de.digitalcollections.cms.server.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
  "de.digitalcollections.cms.server.business.impl.service",
  "de.digitalcollections.cms.server.business.impl.validator"
})
public class SpringConfigBusiness {

}
