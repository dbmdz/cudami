package de.digitalcollections.cudami.admin.config;

import de.digitalcollections.model.api.security.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;

@Configuration
@Order(2)
/*
 * - actuator user ("admin") from application.yml (password unencrypted)
 * - webapp users from database (password bcrypted)
 *
 * see https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-security.html
 * and https://docs.spring.io/spring-security/site/docs/5.1.2.RELEASE/reference/htmlsingle/#multiple-httpsecurity
 */
public class SpringConfigSecurityWebapp extends WebSecurityConfigurerAdapter {

  @Autowired(required = true)
  private UserDetailsService userDetailsService; // provided by component scan

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.authenticationProvider(authProvider());
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // Webapp:
    http.authorizeRequests()
        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
        .antMatchers("/api/**", "/setup/**").permitAll().and().csrf().disable();

    // FIXME: replace with serverside token repository?
    final InMemoryTokenRepositoryImpl inMemoryTokenRepositoryImpl = new InMemoryTokenRepositoryImpl();

    http.authorizeRequests()
            .antMatchers("/users/**").hasAnyAuthority(Role.ADMIN.getAuthority())
            .anyRequest().authenticated().and()
            // enable form based log in
            .formLogin().loginPage("/login").permitAll().and()
            .logout().logoutUrl("/logout").permitAll().and()
            .rememberMe().tokenRepository(inMemoryTokenRepositoryImpl).tokenValiditySeconds(14 * 24 * 3600);
  }

  @Bean
  public AuthenticationProvider authProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setPasswordEncoder(new BCryptPasswordEncoder());
    authProvider.setUserDetailsService(userDetailsService);
    return authProvider;
  }
}
