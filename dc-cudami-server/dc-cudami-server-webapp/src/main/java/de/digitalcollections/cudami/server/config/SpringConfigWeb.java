package de.digitalcollections.cudami.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.commons.springmvc.thymeleaf.SpacesDialect;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import de.digitalcollections.model.xml.xstream.DigitalCollectionsXStreamMarshaller;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.dialect.AbstractProcessorDialect;

@Configuration
@EnableAspectJAutoProxy
//@PropertySource(value = {
//  "classpath:de/digitalcollections/cudami/server/config/SpringConfigWeb-${spring.profiles.active:PROD}.properties"
//})
public class SpringConfigWeb implements WebMvcConfigurer, InitializingBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringConfigWeb.class);

  @Value("${server.port:80}")
  Integer port;

  @Value("${info.app.project.version:unknown}")
  String projectVersion;

  @Override
  public void afterPropertiesSet() throws Exception {
    setupJsondoc();
  }

  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    return new DigitalCollectionsObjectMapper();
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
    configurer
            .favorParameter(true)
            .favorPathExtension(true)
            //            .ignoreAcceptHeader(false)
            .ignoreUnknownPathExtensions(true)
            .useRegisteredExtensionsOnly(false)
            .defaultContentType(MediaType.APPLICATION_JSON); // we are are REST-Server (no HTML/Webapp!) !
    configurer.mediaType("html", MediaType.TEXT_HTML);
    configurer.mediaType("json", MediaType.APPLICATION_JSON);
    configurer.mediaType("xml", MediaType.APPLICATION_XML);
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(createXmlHttpMessageConverter());
  }

  private HttpMessageConverter<Object> createXmlHttpMessageConverter() {
    MarshallingHttpMessageConverter xmlConverter = new MarshallingHttpMessageConverter();
    DigitalCollectionsXStreamMarshaller xstreamMarshaller = new DigitalCollectionsXStreamMarshaller();
    xmlConverter.setMarshaller(xstreamMarshaller);
    xmlConverter.setUnmarshaller(xstreamMarshaller);
    return xmlConverter;
  }

  @Bean
  public AbstractProcessorDialect whiteSpaceNormalizedDialect() {
    return new SpacesDialect();
  }

}
