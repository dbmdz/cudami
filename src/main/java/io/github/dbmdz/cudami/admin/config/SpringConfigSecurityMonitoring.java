package io.github.dbmdz.cudami.admin.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Order(1)
@SuppressFBWarnings(
    value = "THROWS_METHOD_THROWS_CLAUSE_BASIC_EXCEPTION",
    justification = "Spring Security throws java.lang.Exception...")
public class SpringConfigSecurityMonitoring extends WebSecurityConfigurerAdapter {

  @Value("${management.endpoints.web.base-path}")
  private String actuatorBasePath;

  @Value("${spring.security.user.password}")
  private String actuatorPassword;

  @Value("${spring.security.user.name}")
  private String actuatorUsername;

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.inMemoryAuthentication()
        .passwordEncoder(passwordEncoderDummy())
        .withUser(User.withUsername(actuatorUsername).password(actuatorPassword).roles("ACTUATOR"));
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // Monitoring:
    // see
    // https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#production-ready-endpoints
    http.antMatcher(actuatorBasePath + "/**")
        .authorizeRequests()
        .requestMatchers(EndpointRequest.to(HealthEndpoint.class, InfoEndpoint.class))
        .permitAll()
        .requestMatchers(EndpointRequest.to("prometheus", "version"))
        .permitAll()
        .requestMatchers(EndpointRequest.toAnyEndpoint())
        .hasRole("ACTUATOR")
        .and()
        .httpBasic();
  }

  private PasswordEncoder passwordEncoderDummy() {
    return new PasswordEncoder() {
      @Override
      public String encode(CharSequence rawPassword) {
        return rawPassword.toString();
      }

      @Override
      public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return rawPassword.toString().equals(encodedPassword);
      }
    };
  }
}
