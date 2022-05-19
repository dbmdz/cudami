package de.digitalcollections.cudami.server.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class SpringConfigSecurity extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // make it possible to render HTML response from API in an iframe
    http.headers().frameOptions().disable();

    // CSRF handling is 'on' by default with Spring Security
    // see
    // https://stackoverflow.com/questions/38108357/how-to-enable-post-put-and-delete-methods-in-spring-security
    // https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-security.html#boot-features-security-csrf
    // https://docs.spring.io/spring-security/site/docs/5.0.4.RELEASE/reference/htmlsingle/#csrf
    http.csrf().ignoringAntMatchers("/latest/**", "/v1/**", "/v2/**", "/v3/**", "/v5/**", "/v6/**");

    http.authorizeRequests()
        .antMatchers("/latest/**", "/v1/**", "/v2/**", "/v3/**", "/v5/**", "/v6/**")
        .permitAll()
        .requestMatchers(EndpointRequest.to(HealthEndpoint.class, InfoEndpoint.class))
        .permitAll()
        .requestMatchers(EndpointRequest.to("prometheus", "version"))
        .permitAll()
        .requestMatchers(EndpointRequest.toAnyEndpoint())
        .hasRole("ACTUATOR")
        .and()
        .httpBasic();
  }
}
