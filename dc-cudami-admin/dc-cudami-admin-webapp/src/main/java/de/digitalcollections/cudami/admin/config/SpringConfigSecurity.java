package de.digitalcollections.cudami.admin.config;

import de.digitalcollections.model.api.security.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

@Configuration
/*
 FIXME: It is not possible to use two separated user bases.
 what we achieved is:
 - actuator user ("admin") from application.yml (password unencrypted)
 - webapp users from database (password bcrypted)
 BUT: both authentication providers are asked if a user tries to login, so
 - actuator user "admin" is able to login into webapp (!!!) (but sees not secured parts as of missing role...)
 - webapp user is able to authenticate at actuator endpoints (but gets an 403 Unauthorized because of missing ACTUATOR role...)

 see https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-security.html
 */
public class SpringConfigSecurity extends WebSecurityConfigurerAdapter {

  @Value("${spring.security.user.name}")
  private String actuatorUsername;

  @Value("${spring.security.user.password}")
  private String actuatorPassword;

  @Autowired(required = true)
  PersistentTokenRepository persistentTokenRepository;
  @Autowired(required = true)
  private UserDetailsService userDetailsService; // provided by component scan

  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    auth
            .inMemoryAuthentication().passwordEncoder(passwordEncoderDummy()).withUser(User.withUsername(actuatorUsername).password(actuatorPassword).roles("ACTUATOR")).and()
            .authenticationProvider(authProvider());
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // Monitoring:
    // see https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#production-ready-endpoints
    http.authorizeRequests()
            .requestMatchers(EndpointRequest.to("info", "health")).permitAll()
            .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ACTUATOR").and().httpBasic();

    // Webapp:
    http.authorizeRequests()
            .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
            .antMatchers("/api/**", "/setup/**").permitAll();

    http.authorizeRequests()
            .antMatchers("/users/**").hasAnyAuthority(Role.ADMIN.getAuthority())
            .anyRequest().authenticated().and()
            // enable form based log in
            .formLogin().loginPage("/login").permitAll().and()
            .logout().logoutUrl("/logout").permitAll().and()
            .rememberMe().tokenRepository(persistentTokenRepository).tokenValiditySeconds(14 * 24 * 3600);
//            .userDetailsService(userDetailsService);
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

  @Bean
  public AuthenticationProvider authProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setPasswordEncoder(new BCryptPasswordEncoder());
    authProvider.setUserDetailsService(userDetailsService);
    return authProvider;
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

  // FIXME: replace with serverside token repository?
  @Bean
  public PersistentTokenRepository persistentTokenRepository() {
    return new InMemoryTokenRepositoryImpl();
  }

}
