package de.digitalcollections.cudami.admin.test.config;

import de.digitalcollections.cudami.admin.business.api.service.security.UserService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/** Services context. */
@Configuration
public class SpringConfigBusinessForTest {

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
