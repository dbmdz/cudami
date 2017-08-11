package de.digitalcollections.cudami.client.config;

import de.digitalcollections.cudami.model.api.security.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

@Configuration
@Order(1)
public class SpringConfigSecurityWebapp extends WebSecurityConfigurerAdapter {

  @Autowired(required = true)
  PersistentTokenRepository persistentTokenRepository;
  @Autowired(required = true)
  private UserDetailsService userDetailsService; // provided by component scan

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.inMemoryAuthentication().withUser("monitoring").password("secret").roles("ACTUATOR");
    auth.authenticationProvider(authProvider());
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests().antMatchers("/api/**").permitAll();
    http.authorizeRequests().antMatchers("/users/**").hasAnyAuthority(Role.ADMIN.getAuthority());
    http.authorizeRequests().anyRequest().authenticated().and().formLogin() // enable form based log in
            .loginPage("/login").permitAll().and()
            .logout().logoutUrl("/logout").permitAll().and()
            .rememberMe().tokenRepository(persistentTokenRepository).tokenValiditySeconds(14 * 24 * 3600)
            .userDetailsService(userDetailsService).and()
            .httpBasic();
    // FIXME: add CSRF protection and test all static resources and unit tests (if /css is only web.ignoring()...)
    //    // http.csrf().disable();
    //    http.csrf().requireCsrfProtectionMatcher(new RequestMatcher() {
    //      private final Pattern allowedMethods = Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");
    //      private final RegexRequestMatcher apiMatcher = new RegexRequestMatcher("/api/.*", null);
    //      private final RegexRequestMatcher actuatorMatcher = new RegexRequestMatcher("/env.*", null);
    //      private final RegexRequestMatcher uploadsMatcher = new RegexRequestMatcher("/resources/upload", "POST", true);
    //      private final RegexRequestMatcher uploadsPreviewMatcher = new RegexRequestMatcher("/resources/upload/.*?/preview", "GET", true);
    //      private final RegexRequestMatcher sitemapMatcher = new RegexRequestMatcher("/websites/\\d+/sitemap/do.*", null);
    //
    //      @Override
    //      public boolean matches(HttpServletRequest request) {
    //        // No CSRF due to allowedMethod
    //        if (allowedMethods.matcher(request.getMethod()).matches()) {
    //          return false;
    //        }
    //
    //        // No CSRF due to api/upload call
    //        if (actuatorMatcher.matches(request) || apiMatcher.matches(request) || uploadsMatcher.matches(request) || uploadsPreviewMatcher.matches(request)
    //                || sitemapMatcher.matches(request)) {
    //          return false;
    //        }
    //
    //        // CSRF for everything else that is not an API or upload call or an allowedMethod
    //        return true;
    //      }
    //    });
  }

  //@Bean
  public AuthenticationProvider authProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setPasswordEncoder(passwordEncoder());
    authProvider.setUserDetailsService(userDetailsService);
    return authProvider;
  }

  @Bean
  public Object passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // FIXME: replace with serverside token repository?
  @Bean
  public PersistentTokenRepository persistentTokenRepository() {
    return new InMemoryTokenRepositoryImpl();
  }

}
