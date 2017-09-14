package de.digitalcollections.cudami.client.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@Order(2)
public class SpringConfigSecurityMonitoring extends WebSecurityConfigurerAdapter {

  SpringConfigSecurityMonitoring() {
    super(true);
  }

  // TODO: now moved to common authenticationmanagerbuilder in SpringConfigSecurityWebapp, because no separate AuthenticationManagerBuilder seem to be possible. So users of both databases are aggregated for now...
//  @Override
//  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//    auth.inMemoryAuthentication().withUser("monitoring").password("secret").roles("ACTUATOR");
//  }
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests().antMatchers("/manage**").hasRole("ACTUATOR").and().httpBasic();
  }
}
