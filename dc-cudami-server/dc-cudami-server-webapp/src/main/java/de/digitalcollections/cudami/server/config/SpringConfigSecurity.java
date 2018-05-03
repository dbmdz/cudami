package de.digitalcollections.cudami.server.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class SpringConfigSecurity extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // CSRF handling is 'on' by default with Spring Security
    // see https://stackoverflow.com/questions/38108357/how-to-enable-post-put-and-delete-methods-in-spring-security
    // https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-security.html#boot-features-security-csrf
    // https://docs.spring.io/spring-security/site/docs/5.0.4.RELEASE/reference/htmlsingle/#csrf
    http.csrf().ignoringAntMatchers("/v1/**");

    http.authorizeRequests()
            .antMatchers("/v1/**").permitAll()
            .requestMatchers(EndpointRequest.to("info", "health")).permitAll()
            .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ACTUATOR")
            .and()
            .httpBasic();
  }

}
