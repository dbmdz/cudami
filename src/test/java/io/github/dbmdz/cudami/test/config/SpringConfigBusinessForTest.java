package io.github.dbmdz.cudami.test.config;

import io.github.dbmdz.cudami.business.api.service.security.UserService;
import io.github.dbmdz.cudami.business.impl.service.security.UserDetailsServiceImpl;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;

/** Services context. */
@Configuration
public class SpringConfigBusinessForTest {

  @Bean
  public UserDetailsService userDetailsService(UserService userService) {
    return new UserDetailsServiceImpl(userService);
  }

  @Primary
  @Bean
  public UserService userService() {
    final UserService userservice = Mockito.mock(UserService.class);
    //    Mockito.when(userservice.doesActiveAdminUserExist()).thenReturn(Boolean.TRUE);
    //    User user = new User("admin", "secret", true, true, true, true, null);
    //    Mockito.when(userservice.loadUserByUsername(Mockito.anyString())).thenReturn(user);
    return userservice;
  }
}
