package io.github.dbmdz.cudami.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SpringConfigSecurityMonitoring {

  @Value("${management.endpoints.web.base-path}")
  private String actuatorBasePath;

  @Value("${spring.security.user.password}")
  private String actuatorPassword;

  @Value("${spring.security.user.name}")
  private String actuatorUsername;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.securityMatcher(actuatorBasePath + "/**")
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(EndpointRequest.to(HealthEndpoint.class, InfoEndpoint.class))
                    .permitAll()
                    .requestMatchers(EndpointRequest.to("prometheus", "version"))
                    .permitAll()
                    .requestMatchers(EndpointRequest.toAnyEndpoint())
                    .hasRole("ACTUATOR"))
        .httpBasic();
    return http.build();
  }

  @Bean
  public UserDetailsService userDetailsService() {
    UserDetails user =
        User.withUsername(actuatorUsername)
            .password(passwordEncoder().encode(actuatorPassword))
            .roles("ACTUATOR")
            .build();
    return username -> user;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }
}
