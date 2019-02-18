package de.digitalcollections.cudami.server.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
  "de.digitalcollections.commons.file.backend.impl",
  "de.digitalcollections.commons.file.business.impl.service"
})
public class SpringConfigBackendFile {

}
