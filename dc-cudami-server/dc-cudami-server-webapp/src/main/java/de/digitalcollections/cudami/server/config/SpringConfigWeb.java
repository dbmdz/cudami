package de.digitalcollections.cudami.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.model.jackson.CudamiObjectMapper;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

  @Value("${server.port:80}")
  Integer port;

  @Value("${info.app.project.version:unknown}")
  String projectVersion;

  @Override
  public void afterPropertiesSet() throws Exception {
    // customize default spring boot jackson objectmapper
    CudamiObjectMapper.customize(objectMapper);

    setupJsondoc();
  }

  private void setupJsondoc() {
    String hostName;
    try {
      InetAddress addr = InetAddress.getLocalHost();
      hostName = addr.getCanonicalHostName();
      if (!hostName.contains(".")) {
        hostName = "localhost";
      }
    } catch (UnknownHostException e) {
      hostName = "localhost";
    }

    System.setProperty("jsondoc.basePath", "http://" + hostName + ":" + port);
    System.setProperty("jsondoc.version", projectVersion);
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
