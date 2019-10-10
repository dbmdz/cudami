package de.digitalcollections.cudami.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"de.digitalcollections.cudami.admin.backend.impl.repository"})
public class SpringConfigBackend {}
