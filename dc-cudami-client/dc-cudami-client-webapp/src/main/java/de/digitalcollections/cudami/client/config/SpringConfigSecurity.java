package de.digitalcollections.cudami.client.config;

import de.digitalcollections.cudami.model.api.security.enums.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

/**
 * Contains Spring Security related configuration.
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SpringConfigSecurity extends WebSecurityConfigurerAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringConfigSecurity.class);

  @Autowired(required = true)
  PersistentTokenRepository persistentTokenRepository;

  @Autowired(required = true)
  private UserDetailsService userDetailsService; // provided by component scan

  // FIXME: replace with serverside token repository?
  @Bean
  public PersistentTokenRepository persistentTokenRepository() {
    return new InMemoryTokenRepositoryImpl();
  }

//  @Bean
  public AuthenticationProvider authProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setPasswordEncoder(passwordEncoder());
    authProvider.setUserDetailsService(userDetailsService);
    return authProvider;
  }

//  @Bean
//  @Override
//  public AuthenticationManager authenticationManagerBean() throws Exception {
//    return super.authenticationManagerBean();
//  }
  @Override
  public void configure(WebSecurity web) throws Exception {
    web.ignoring(); // ignore all URLs that start with /resources/ or /static/*: .antMatchers("/resources/**").antMatchers("/static/**");
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests().anyRequest().permitAll();
    http.authorizeRequests().antMatchers("/manage/**").hasRole("ACTUATOR").and().httpBasic();
    http.authorizeRequests().antMatchers("/users/**").hasAnyAuthority(Role.ADMIN.getAuthority()).and()
            // Possibly more configuration ...
            .formLogin() // enable form based log in
            // set permitAll for all URLs associated with Form Login
            .permitAll();

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
//
//    // actuator endpoints
//    http.authorizeRequests()
//            .antMatchers("/actuator/**/*", "/health/**/*", "/info*", "/env*")
//            .permitAll();
//
//    // Login and error pages must be accessible to all
//    http.authorizeRequests()
//            .antMatchers("/api/**/*", "/cudami/**/*", "/error*", "/oai*", "/object/**", "/login*", "/setup/**", "/uploads/**")
//            .permitAll();
//    // Needed for preview
//    http.authorizeRequests()
//            .antMatchers("/resource/**", "/iiif/**", "/pages/preview/**", "/resources/upload/**/*")
//            .permitAll();
//    // Assets must be accessible to all, too
////    http.authorizeRequests()
////            .antMatchers("/css/**", "/favicon.ico", "/fonts/*", "/images/**", "/js/**", "/less/*", "/webjars/**")
////            .permitAll(); // see configure() method
//    http.authorizeRequests()
//            .antMatchers("/users/**")
//            .hasAnyAuthority(Role.ADMIN.getAuthority());
//    // else: authenticate please
//    http.authorizeRequests()
//            .anyRequest().authenticated()
//            .and().formLogin()
//            .loginPage("/login")
//            .permitAll()
//            .and().logout()
//            .logoutUrl("/logout")
//            .permitAll()
//            .and().rememberMe()
//            .tokenRepository(persistentTokenRepository)
//            // 14 days = 14 * 24 h/d * 3600 s/h = 1209600 s
//            .tokenValiditySeconds(14 * 24 * 3600)
//            .userDetailsService(userDetailsService)
//            .and().httpBasic();
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.authenticationProvider(authProvider());
    auth.inMemoryAuthentication().withUser("monitoring").password("secret").roles("ACTUATOR");
    auth.inMemoryAuthentication().withUser("admin").password("secret").roles(Role.ADMIN.name());
  }

  @Bean
  public Object passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
