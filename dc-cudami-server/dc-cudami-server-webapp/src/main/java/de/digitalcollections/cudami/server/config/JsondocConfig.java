package de.digitalcollections.cudami.server.config;

import de.digitalcollections.cudami.server.interceptor.JsondocInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.MappedInterceptor;

@Configuration
public class JsondocConfig {

  @Bean
  public MappedInterceptor mappedInterceptor(JsondocInterceptor jsondocInterceptor) {
    return new MappedInterceptor(new String[] {"/jsondoc*"}, jsondocInterceptor);
  }
}
