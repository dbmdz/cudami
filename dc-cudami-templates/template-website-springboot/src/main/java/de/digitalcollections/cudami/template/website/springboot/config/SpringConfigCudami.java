package de.digitalcollections.cudami.template.website.springboot.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(
    basePackages = "de.digitalcollections.cudami.client"
)
public class SpringConfigCudami {

}
