package de.digitalcollections.cudami.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.commons.springmvc.thymeleaf.SpacesDialect;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import de.digitalcollections.model.xml.xstream.DigitalCollectionsXStreamMarshaller;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.dialect.AbstractProcessorDialect;

@Configuration
public class SpringConfigWeb implements WebMvcConfigurer {

  @Value("${server.port:80}")
  Integer port;

  @Value("${info.app.project.version:unknown}")
  String projectVersion;

  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    return new DigitalCollectionsObjectMapper();
  }

  @Bean
  public FormattingConversionService conversionService() {
    DefaultFormattingConversionService conversionService =
        new DefaultFormattingConversionService(false);

    // for conversion of String to LocalDate for controller method arguments:
    DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
    registrar.setDateFormatter(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    registrar.setDateTimeFormatter(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
    registrar.registerFormatters(conversionService);

    return conversionService;
  }

  @Override
  public void configurePathMatch(PathMatchConfigurer configurer) {
    // "This is generally recommended to reduce ambiguity and to avoid issues such as when a "."
    // appears in the path for other reasons."
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
        .defaultContentType(
            MediaType.APPLICATION_JSON); // we are are REST-Server (no HTML/Webapp!) !
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
    DigitalCollectionsXStreamMarshaller xstreamMarshaller =
        new DigitalCollectionsXStreamMarshaller();
    xmlConverter.setMarshaller(xstreamMarshaller);
    xmlConverter.setUnmarshaller(xstreamMarshaller);
    return xmlConverter;
  }

  /**
   * Needed to get rid of all the whitespaces in the rendered thymeleaf HTML
   *
   * @return the dialect
   */
  @Bean
  public AbstractProcessorDialect whiteSpaceNormalizedDialect() {
    return new SpacesDialect();
  }
}
