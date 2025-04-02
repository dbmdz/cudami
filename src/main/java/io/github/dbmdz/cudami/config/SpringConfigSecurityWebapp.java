package io.github.dbmdz.cudami.config;

import de.digitalcollections.model.security.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SpringConfigSecurityWebapp {

  @Value("${spring.security.rememberme.secret-key}")
  private String rememberMeSecretKey;

  private final UserDetailsService userDetailsService;

  public SpringConfigSecurityWebapp(UserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
            auth ->
                auth.requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                    .permitAll()
                    .requestMatchers("/api/**", "/setup/**")
                    .permitAll()
                    .requestMatchers("/users/updatePassword")
                    .hasAnyAuthority(Role.ADMIN.getAuthority(), Role.CONTENT_MANAGER.getAuthority())
                    .requestMatchers("/users/**")
                    .hasAnyAuthority(Role.ADMIN.getAuthority())
                    .anyRequest()
                    .authenticated())
        .csrf(csrf -> csrf.disable())
        .formLogin(form -> form.loginPage("/login").permitAll())
        .logout(logout -> logout.logoutUrl("/logout").permitAll())
        .rememberMe(
            rememberMe ->
                rememberMe
                    .rememberMeParameter("remember-me")
                    .key(rememberMeSecretKey)
                    .userDetailsService(userDetailsService)
                    .tokenValiditySeconds(14 * 24 * 3600));

    return http.build();
  }

  @Bean
  public AuthenticationProvider authProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setPasswordEncoder(passwordEncoder());
    authProvider.setUserDetailsService(userDetailsService);
    return authProvider;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
