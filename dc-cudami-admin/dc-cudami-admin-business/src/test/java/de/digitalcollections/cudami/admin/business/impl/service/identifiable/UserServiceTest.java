package de.digitalcollections.cudami.admin.business.impl.service.identifiable;

import de.digitalcollections.cudami.admin.backend.api.repository.security.UserRepository;
import de.digitalcollections.cudami.admin.business.api.service.security.UserService;
import de.digitalcollections.cudami.config.SpringConfigBackendForTest;
import de.digitalcollections.cudami.config.SpringConfigBusiness;
import de.digitalcollections.cudami.model.api.security.User;
import de.digitalcollections.cudami.model.impl.security.UserImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpringConfigBusiness.class, SpringConfigBackendForTest.class})
public class UserServiceTest {

  private User user;

  @Autowired
  UserRepository userRepository;

  @Autowired
  UserService service;

  @Before
  public void setup() {
    user = new UserImpl();
    user.setEmail("foo@spar.org");
    user.setPasswordHash(new BCryptPasswordEncoder().encode("foobar"));
    Mockito.when(userRepository.findByEmail("foo@spar.org")).thenReturn(user);
  }

  @After
  public void tearDown() {
    Mockito.reset(userRepository);
  }

  @Test
  public void testLoadUserByUsername() throws Exception {
    UserDetails retrieved = service.loadUserByUsername("foo@spar.org");
    assertEquals(user.getEmail(), retrieved.getUsername());
    Mockito.verify(userRepository, VerificationModeFactory.times(1)).findByEmail("foo@spar.org");
  }

  @Test
  public void testGetPasswordHash() throws Exception {
    PasswordEncoder pwEncoder = new BCryptPasswordEncoder();
    assertTrue(pwEncoder.matches("foobar", user.getPasswordHash()));
  }
}
