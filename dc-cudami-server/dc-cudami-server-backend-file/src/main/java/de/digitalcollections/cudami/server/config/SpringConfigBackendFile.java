package de.digitalcollections.cudami.server.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/** File repository configuration. */
@Configuration
@ComponentScan(basePackages = {"de.digitalcollections.cudami.server.backend.impl.file"})
public class SpringConfigBackendFile {}
