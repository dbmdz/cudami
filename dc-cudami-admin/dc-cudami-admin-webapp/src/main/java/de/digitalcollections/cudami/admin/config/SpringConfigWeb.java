package de.digitalcollections.cudami.admin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mxab.thymeleaf.extras.dataattribute.dialect.DataAttributeDialect;
import de.digitalcollections.commons.servlet.filter.LogSessionIdFilter;
import de.digitalcollections.commons.springmvc.config.SpringConfigCommonsMvc;
import de.digitalcollections.commons.springmvc.controller.ErrorController;
import de.digitalcollections.commons.springmvc.thymeleaf.SpacesDialect;
import de.digitalcollections.cudami.admin.converter.GrantedAuthorityJsonFilter;
import de.digitalcollections.cudami.admin.interceptors.CreateAdminUserInterceptor;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.dialect.springdata.SpringDataDialect;
import org.thymeleaf.extras.springsecurity5.dialect.SpringSecurityDialect;
import org.yaml.snakeyaml.Yaml;

@Configuration
@ComponentScan(
    basePackages = {
      "de.digitalcollections.commons.springmvc.controller"
    }, excludeFilters = {
      @ComponentScan.Filter(value = ErrorController.class, type = FilterType.ASSIGNABLE_TYPE)
    }
)
@EnableAspectJAutoProxy
@EnableSpringDataWebSupport // for getting support for sorting and paging params
@Import(SpringConfigCommonsMvc.class)
public class SpringConfigWeb implements WebMvcConfigurer {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringConfigWeb.class);

  static final String ENCODING = "UTF-8";

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
//    registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    registry.addResourceHandler("/favicon.ico").addResourceLocations("classpath:/static/images/favicon.png");
//    registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
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
  public SpringDataDialect springDataDialect() {
    return new SpringDataDialect();
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

    InterceptorRegistration createAdminUserInterceptorRegistration = registry.addInterceptor(createAdminUserInterceptor());
    createAdminUserInterceptorRegistration.addPathPatterns("/login");
  }

  @Bean
  public SessionLocaleResolver localeResolver() {
    return new SessionLocaleResolver();
  }

  @Bean
  public CreateAdminUserInterceptor createAdminUserInterceptor() {
    return new CreateAdminUserInterceptor();
  }

  @Override
  public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
    configurer.enable();
  }

  @Bean
  public FilterRegistrationBean logSessionIdFilter() {
    FilterRegistrationBean registration = new FilterRegistrationBean();
    registration.setFilter(new LogSessionIdFilter());
    // In case you want the filter to apply to specific URL patterns only (defaults to "/*")
    registration.addUrlPatterns("/*");
    return registration;
  }

  @Bean
  public Map<String, String> webjarVersions() {
    Map<String, String> versions;
    Yaml yaml = new Yaml();
    try (InputStream in = this.getClass().getResourceAsStream("/webjar-versions.yml")) {
      versions = (Map<String, String>) yaml.load(in);
    } catch (IOException exception) {
      throw new IllegalStateException(exception);
    }
    return versions;
  }
}
