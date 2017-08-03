package de.digitalcollections.cudami.server.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.core.model.jackson.DcCoreModelModule;
import de.digitalcollections.cudami.model.jackson.CudamiModule;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableAspectJAutoProxy
//@PropertySource(value = {
//  "classpath:de/digitalcollections/cudami/server/config/SpringConfigWeb-${spring.profiles.active:PROD}.properties"
//})
public class SpringConfigWeb extends WebMvcConfigurerAdapter implements InitializingBean {

  @Autowired
  ObjectMapper objectMapper;

  @Override
  public void afterPropertiesSet() throws Exception {
    // customize default spring boot jackson objectmapper
    objectMapper.registerModule(new DcCoreModelModule());
    objectMapper.registerModule(new CudamiModule());
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

//  @Bean
//  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
//    return new PropertySourcesPlaceholderConfigurer();
//  }
//  @Primary // needed to replace default spring boot created object mapper
//  @Bean
//  public ObjectMapper objectMapper() {
//    ObjectMapper objectMapper = new JacksonModelObjectMapper();
//    return objectMapper;
//  }
  @Override
  public void configurePathMatch(PathMatchConfigurer configurer) {
    // "This is generally recommended to reduce ambiguity and to avoid issues such as when a "." appears in the path for other reasons."
    configurer.setUseRegisteredSuffixPatternMatch(Boolean.TRUE);
  }

  @Override
  public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    configurer.defaultContentType(MediaType.APPLICATION_JSON); // we are are REST-Server (no HTML/Webapp!) !
    configurer.mediaType("html", MediaType.TEXT_HTML);
    configurer.mediaType("json", MediaType.APPLICATION_JSON);
  }

}
