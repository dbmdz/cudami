package de.digitalcollections.cudami.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

// @Configuration
public class RequestLoggingFilterConfig {

  @Bean
  public CommonsRequestLoggingFilter logFilter() {
    CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
    filter.setIncludeHeaders(true);
    filter.setIncludeQueryString(true);
    filter.setIncludePayload(true);
    //    filter.setMaxPayloadLength(10000);
    filter.setAfterMessagePrefix("REQUEST DATA : ");
    return filter;
  }
}
