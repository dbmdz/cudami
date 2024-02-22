package io.github.dbmdz.cudami.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mxab.thymeleaf.extras.dataattribute.dialect.DataAttributeDialect;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import io.github.dbmdz.cudami.converter.GrantedAuthorityJsonFilter;
import io.github.dbmdz.cudami.converter.StringToOrderConverter;
import io.github.dbmdz.cudami.interceptors.CreateAdminUserInterceptor;
import io.github.dbmdz.cudami.interceptors.RequestIdLoggingInterceptor;
import io.github.dbmdz.cudami.servlet.filter.LogSessionIdFilter;
import io.github.dbmdz.cudami.thymeleaf.SpacesDialect;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.format.FormatterRegistry;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.extras.springsecurity5.dialect.SpringSecurityDialect;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Configuration
public class SpringConfigWeb implements WebMvcConfigurer {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringConfigWeb.class);

  static final String ENCODING = "UTF-8";

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry
        .addResourceHandler("/favicon.ico")
        .addResourceLocations("classpath:/static/images/favicon.png");
  }

  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    DigitalCollectionsObjectMapper objectMapper = new DigitalCollectionsObjectMapper();
    objectMapper.addMixIn(GrantedAuthority.class, GrantedAuthorityJsonFilter.class);
    return objectMapper;
  }

  @Bean
  public DataAttributeDialect dataAttributeDialect() {
    return new DataAttributeDialect();
  }

  @Bean
  public LayoutDialect layoutDialect() {
    return new LayoutDialect();
  }

  @Bean
  public AbstractProcessorDialect whiteSpaceNormalizedDialect() {
    return new SpacesDialect();
  }

  @Bean
  public SpringSecurityDialect springSecurityDialect() {
    return new SpringSecurityDialect();
  }

  @Bean
  public LocaleChangeInterceptor localeChangeInterceptor() {
    LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
    localeChangeInterceptor.setParamName("language");
    return localeChangeInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(localeChangeInterceptor());
    registry.addInterceptor(new RequestIdLoggingInterceptor());

    InterceptorRegistration createAdminUserInterceptorRegistration =
        registry.addInterceptor(createAdminUserInterceptor());
    createAdminUserInterceptorRegistration.addPathPatterns("/login");
  }

  @Bean
  public SessionLocaleResolver localeResolver() {
    return new SessionLocaleResolver();
  }

  @Bean(name = "CommonsClasspathThymeleafResolver")
  public ClassLoaderTemplateResolver commonsClassLoaderThymeleafTemplateResolver() {
    ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
    templateResolver.setPrefix("/de/digitalcollections/commons/springmvc/thymeleaf/templates/");
    templateResolver.setSuffix(".html");
    templateResolver.setCheckExistence(true);
    templateResolver.setCharacterEncoding("UTF-8");
    templateResolver.setTemplateMode(TemplateMode.HTML);
    return templateResolver;
  }

  @Bean
  public CreateAdminUserInterceptor createAdminUserInterceptor() {
    return new CreateAdminUserInterceptor();
  }

  @Bean
  public FilterRegistrationBean logSessionIdFilter() {
    FilterRegistrationBean registration = new FilterRegistrationBean();
    registration.setFilter(new LogSessionIdFilter());
    // In case you want the filter to apply to specific URL patterns only (defaults to "/*")
    registration.addUrlPatterns("/*");
    return registration;
  }

  /**
   * Create a resource bundle for your messages ("messages.properties"). This file goes in
   * src/main/resources because you want it to appear at the root of the classpath on deployment.
   *
   * @return message source
   */
  @Bean(name = "messageSource")
  public MessageSource messageSource() {
    ReloadableResourceBundleMessageSource messageSource =
        new ReloadableResourceBundleMessageSource();
    messageSource.setBasenames("classpath:messages", "classpath:messages-cudami-client");
    messageSource.setCacheSeconds(600);
    messageSource.setDefaultEncoding("UTF-8");
    return messageSource;
  }

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(new StringToOrderConverter());
  }
}
