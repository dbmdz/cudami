package de.digitalcollections.cudami.client.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mxab.thymeleaf.extras.dataattribute.dialect.DataAttributeDialect;
import de.digitalcollections.commons.servlet.filter.LogSessionIdFilter;
import de.digitalcollections.commons.springmvc.config.SpringConfigCommonsMvc;
import de.digitalcollections.commons.springmvc.controller.ErrorController;
import de.digitalcollections.cudami.client.converter.GrantedAuthorityJsonFilter;
import de.digitalcollections.cudami.client.converter.UserJsonFilter;
import de.digitalcollections.cudami.client.interceptors.CreateAdminUserInterceptor;
import de.digitalcollections.cudami.model.api.security.User;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.thymeleaf.dialect.springdata.SpringDataDialect;
import org.thymeleaf.extras.springsecurity4.dialect.SpringSecurityDialect;

@Configuration
@ComponentScan(basePackages = {
  //  "de.digitalcollections.cudami.client.webapp.aop",
  //  "de.digitalcollections.cudami.client.webapp.controller",
  //  "de.digitalcollections.cudami.client.webapp.propertyeditor",
  "de.digitalcollections.commons.springmvc.controller"}, excludeFilters = {
  @ComponentScan.Filter(value = ErrorController.class, type = FilterType.ASSIGNABLE_TYPE)}
)
@EnableAspectJAutoProxy
@EnableSpringDataWebSupport // for getting support for sorting and paging params
//@PropertySource(value = {
//  "classpath:de/digitalcollections/cudami/config/SpringConfigWeb-${spring.profiles.active:local}.properties"
//})
@Import(SpringConfigCommonsMvc.class)
public class SpringConfigWeb extends WebMvcConfigurerAdapter {

  static final String ENCODING = "UTF-8";

//  private ApplicationContext applicationContext;
//  @Bean
//  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
//    return new PropertySourcesPlaceholderConfigurer();
//  }
//  @Value("${cacheTemplates}")
//  private boolean cacheTemplates;
//  @Autowired
//  @Qualifier("CommonsClasspathThymeleafResolver")
//  private ClassLoaderTemplateResolver commonsClasspathThymeleafResolver;
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
//    registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    registry.addResourceHandler("/favicon.ico").addResourceLocations("classpath:/static/images/favicon.png");
    registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
  }

//  @Override
//  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//    this.applicationContext = applicationContext;
//  }
//  @Bean
//  public SpringResourceTemplateResolver springResourceTemplateResolver() {
//    SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
//    templateResolver.setApplicationContext(applicationContext);
//    templateResolver.setPrefix("/WEB-INF/templates/");
//    templateResolver.setSuffix(".html");
//    templateResolver.setCheckExistence(true);
//    templateResolver.setCharacterEncoding(ENCODING);
//    templateResolver.setTemplateMode(TemplateMode.HTML);
//    templateResolver.setCacheable(cacheTemplates);
//    return templateResolver;
//  }
//  @Bean
//  public SpringTemplateEngine templateEngine() {
//    SpringTemplateEngine templateEngine = new SpringTemplateEngine();
//    templateEngine.setEnableSpringELCompiler(true);
//
//    commonsClasspathThymeleafResolver.setOrder(1);
//    SpringResourceTemplateResolver springResourceTemplateResolver = springResourceTemplateResolver();
//    springResourceTemplateResolver.setOrder(2);
//    templateEngine.addTemplateResolver(commonsClasspathThymeleafResolver);
//    templateEngine.addTemplateResolver(springResourceTemplateResolver);
//
//    // Activate Thymeleaf LayoutDialect[1] (for 'layout'-namespace)
//    // [1] https://github.com/ultraq/thymeleaf-layout-dialect
//    templateEngine.addDialect(new LayoutDialect());
//    templateEngine.addDialect(new SpringSecurityDialect());
//    templateEngine.addDialect(new DataAttributeDialect());
//    templateEngine.addDialect(new SpringDataDialect());
//    return templateEngine;
//  }
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
  public SpringSecurityDialect springSecurityDialect() {
    return new SpringSecurityDialect();
  }

//  @Bean
//  public ViewResolver viewResolver() {
//    ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
//    viewResolver.setTemplateEngine(templateEngine());
//    viewResolver.setOrder(1);
//    viewResolver.setCharacterEncoding(ENCODING);
//
//    return viewResolver;
//  }
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
    localeChangeInterceptor.setParamName("language");
    registry.addInterceptor(localeChangeInterceptor);

    InterceptorRegistration createAdminUserInterceptorRegistration = registry.addInterceptor(createAdminUserInterceptor());
    createAdminUserInterceptorRegistration.addPathPatterns("/login");
  }

  @Bean(name = "localeResolver")
  public LocaleResolver sessionLocaleResolver() {
    SessionLocaleResolver localeResolver = new SessionLocaleResolver();
    localeResolver.setDefaultLocale(Locale.GERMAN);
    return localeResolver;
  }

  @Bean
  public CreateAdminUserInterceptor createAdminUserInterceptor() {
    return new CreateAdminUserInterceptor();
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    // support for @ResponseBody of type String
    final StringHttpMessageConverter stringHMC = new StringHttpMessageConverter(Charset.forName(ENCODING));
    // supported MediaTypes for stringHMC are by default set to: "text/plain" and MediaType.ALL
    converters.add(stringHMC);

    // support for @ResponseBody of type Object: convert object to JSON
    // used in ApiController
    converters.add(mappingJackson2HttpMessageConverter());

    // support for @ResponseBody of type byte[]
    ByteArrayHttpMessageConverter bc = new ByteArrayHttpMessageConverter();
    List<MediaType> supported = new ArrayList<>();
    supported.add(MediaType.IMAGE_JPEG);
    supported.add(MediaType.IMAGE_GIF);
    supported.add(MediaType.IMAGE_PNG);
    bc.setSupportedMediaTypes(supported);
    converters.add(bc);
  }

  @Bean
  public HttpMessageConverter<?> mappingJackson2HttpMessageConverter() {
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
    List<MediaType> supportedMediaTypes = new ArrayList<>();
    supportedMediaTypes.add(MediaType.APPLICATION_JSON);
    converter.setSupportedMediaTypes(supportedMediaTypes);
    converter.setObjectMapper(objectMapper());
    return converter;
  }

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    // do not serialize null values/objects
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    // define which fields schould be ignored with Filter-classes:
    objectMapper.addMixIn(User.class, UserJsonFilter.class);
    objectMapper.addMixIn(GrantedAuthority.class, GrantedAuthorityJsonFilter.class);
    return objectMapper;
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
    //    registration.addUrlPatterns("/*");
    return registration;
  }
}
