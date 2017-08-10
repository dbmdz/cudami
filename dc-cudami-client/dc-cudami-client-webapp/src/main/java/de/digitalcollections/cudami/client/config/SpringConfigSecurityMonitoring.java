package de.digitalcollections.cudami.client.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@Order(value = 2)
public class SpringConfigSecurityMonitoring extends WebSecurityConfigurerAdapter {

  public SpringConfigSecurityMonitoring() {
    super();
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.inMemoryAuthentication().withUser("monitoring").password("secret").roles("ACTUATOR");
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests().antMatchers("/manage**").hasRole("ACTUATOR").and().httpBasic();
  }

}
