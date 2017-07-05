package de.digitalcollections.cms.server.backend.impl.jpa.repository;

import de.digitalcollections.cms.config.SpringConfigBackendForTest;
import de.digitalcollections.cms.model.api.security.User;
import de.digitalcollections.cms.server.backend.api.repository.UserRepository;
import de.digitalcollections.cms.server.backend.impl.jpa.entity.TestUserFactory;
import de.digitalcollections.cms.server.backend.impl.jpa.entity.UserImplJpa;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpringConfigBackendForTest.class})
@Transactional
public class UserRepositoryImplJpaTest {

  @Autowired
  private UserRepository repository;

  @Test
  @Rollback(true)
  public void testSaveAndFind() {
    UserImplJpa entity = new UserImplJpa();
    repository.save(entity);
    User user = (User) repository.findOne(entity.getId());

    assertNotNull(user);
    assertEquals(entity.getId(), user.getId());
  }

  @Test
  @Rollback(true)
  public void testSaveAndDelete() throws Exception {
    UserImplJpa entity = TestUserFactory.build("test@test.org");
    repository.save(entity);
    User foundEntity = (User) repository.findOne(entity.getId());
    assertEquals(entity.getId(), foundEntity.getId());

    repository.delete(foundEntity);

    foundEntity = (User) repository.findOne(entity.getId());
    Assert.assertTrue(foundEntity == null);
  }

  @Test
  @Rollback(true)
  public void testFindByEmail() throws Exception {
    UserImplJpa entity = TestUserFactory.build("test@test.org");
    repository.save(entity);
    User user = repository.findByEmail("test@test.org");
    assertEquals(entity.getId(), user.getId());
  }
}
