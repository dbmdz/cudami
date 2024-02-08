package de.digitalcollections.cudami.admin.business.impl.service.identifiable;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.admin.business.impl.service.security.UserServiceImpl;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.security.CudamiUsersClient;
import de.digitalcollections.model.security.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserServiceTest {

  private final CudamiClient cudamiClient = Mockito.mock(CudamiClient.class);
  private final MessageSource messageSource = Mockito.mock(MessageSource.class);
  private UserServiceImpl service;
  private User user;
  private final CudamiUsersClient userRepository = Mockito.mock(CudamiUsersClient.class);

  @BeforeEach
  public void setup() throws Exception {
    user = new User();
    user.setEmail("foo@spar.org");
    user.setPasswordHash(new BCryptPasswordEncoder().encode("foobar"));
    Mockito.when(cudamiClient.forUsers()).thenReturn(userRepository);
    Mockito.when(messageSource.getMessage(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn("foobar");
    Mockito.when(userRepository.getByEmail("foo@spar.org")).thenReturn(user);
    service = new UserServiceImpl(null, cudamiClient, messageSource);
  }

  @AfterEach
  public void tearDown() {
    Mockito.reset(userRepository);
  }

  @Test
  public void testGetByEmail() throws Exception {
    User retrieved = service.getByEmail("foo@spar.org");
    assertThat(retrieved.getEmail()).isEqualTo(user.getEmail());
    Mockito.verify(userRepository, VerificationModeFactory.times(1)).getByEmail("foo@spar.org");
  }

  @Test
  public void testGetPasswordHash() throws Exception {
    PasswordEncoder pwEncoder = new BCryptPasswordEncoder();
    assertThat(pwEncoder.matches("foobar", user.getPasswordHash())).isTrue();
  }
}
