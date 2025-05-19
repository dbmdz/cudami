package io.github.dbmdz.cudami.config;

import io.github.dbmdz.cudami.model.security.Role;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SpringConfigSecurityWebapp {

  @Value("${spring.security.rememberme.secret-key}")
  private String rememberMeSecretKey;

  @Bean
  @Order(0)
  SecurityFilterChain actuatorFilterChain(HttpSecurity http) throws Exception {
    http.securityMatcher(EndpointRequest.toAnyEndpoint())
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
    return http.build();
  }

  @Bean
  @Order(1)
  SecurityFilterChain webappFilterChain(
      HttpSecurity http, @Qualifier("userDetailsServiceImpl") UserDetailsService userDetailsService)
      throws Exception {
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
  public AuthenticationProvider authProvider(
      @Qualifier("userDetailsServiceImpl") UserDetailsService userDetailsService) {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setPasswordEncoder(new BCryptPasswordEncoder());
    authProvider.setUserDetailsService(userDetailsService);
    return authProvider;
  }
}
