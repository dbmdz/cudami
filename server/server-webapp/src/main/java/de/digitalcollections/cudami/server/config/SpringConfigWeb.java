package de.digitalcollections.cudami.server.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@ComponentScan(basePackages = {
  "de.digitalcollections.cudami.server.webapp.controller"
})
@EnableAspectJAutoProxy
@EnableWebMvc
//@PropertySource(value = {
//  "classpath:de/digitalcollections/cms/server/config/SpringConfigWeb-${spring.profiles.active:PROD}.properties"
//})
public class SpringConfigWeb extends WebMvcConfigurerAdapter {

//  @Bean
//  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
//    return new PropertySourcesPlaceholderConfigurer();
//  }
}
