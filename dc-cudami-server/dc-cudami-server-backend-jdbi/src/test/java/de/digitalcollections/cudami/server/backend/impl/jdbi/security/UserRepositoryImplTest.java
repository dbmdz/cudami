package de.digitalcollections.cudami.server.backend.impl.jdbi.security;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendTestDatabase;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.security.Role;
import de.digitalcollections.model.security.User;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    classes = UserRepositoryImpl.class)
@ContextConfiguration(classes = SpringConfigBackendTestDatabase.class)
@DisplayName("The User Repository")
@Sql(
    scripts = {"classpath:cleanup_database.sql"},
    executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
public class UserRepositoryImplTest {

  UserRepositoryImpl repo;

  @Autowired Jdbi jdbi;

  @Autowired CudamiConfig cudamiConfig;

  @BeforeEach
  public void beforeEach() {
    repo = new UserRepositoryImpl(jdbi, cudamiConfig);
  }

  @Test
  @DisplayName("should return count of records in table")
  public void testCount() throws RepositoryException {
    User user1 =
        User.builder().email("home@simpson.de").firstname("Homer").lastname("Simpson").build();
    repo.save(user1);
    UUID uuid1 = user1.getUuid();

    User user2 =
        User.builder().email("marge@simpson.de").firstname("Marjorie").lastname("Simpson").build();
    repo.save(user2);
    UUID uuid2 = user2.getUuid();

    long count = repo.count();
    assertThat(count).isEqualTo(2);

    repo.deleteByUuids(List.of(uuid1, uuid2));
  }

  @Test
  @DisplayName("can find users")
  void find() throws RepositoryException {
    User user1 = new User();
    user1.setEmail("homer@simpson.de");
    user1.setFirstname("Homer");
    user1.setLastname("Simpson");
    repo.save(user1);

    User user2 = new User();
    user2.setEmail("marge@simpson.de");
    user2.setFirstname("Marjorie");
    user2.setLastname("Simpson");
    repo.save(user2);

    User user3 = new User();
    user3.setEmail("wiggum@police-springfield.gov");
    user3.setFirstname("Clancy");
    user3.setLastname("Wiggum");
    repo.save(user3);

    Filtering filtering =
        Filtering.builder()
            .add(FilterCriterion.builder().withExpression("email").contains("simpson.de").build())
            .build();
    PageRequest pageRequest =
        PageRequest.builder().pageNumber(0).pageSize(99).filtering(filtering).build();
    PageResponse<User> pageResponse = repo.find(pageRequest);
    List<User> actualContent = pageResponse.getContent();
    assertThat(actualContent).hasSize(2);
    User actual = actualContent.get(0);
    assertThat(actual.getEmail()).isEqualTo(user1.getEmail());
    assertThat(actual.getFirstname()).isEqualTo(user1.getFirstname());
    assertThat(actual.getLastname()).isEqualTo(user1.getLastname());

    filtering =
        Filtering.builder()
            .add(FilterCriterion.builder().withExpression("firstname").contains("marjorie").build())
            .build();
    pageRequest.setFiltering(filtering);
    pageResponse = repo.find(pageRequest);
    actualContent = pageResponse.getContent();
    assertThat(actualContent).hasSize(1);
    actual = actualContent.get(0);
    assertThat(actual.getEmail()).isEqualTo(user2.getEmail());
    assertThat(actual.getFirstname()).isEqualTo(user2.getFirstname());
    assertThat(actual.getLastname()).isEqualTo(user2.getLastname());

    filtering =
        Filtering.builder()
            .add(FilterCriterion.builder().withExpression("lastname").contains("Wiggum").build())
            .build();
    pageRequest.setFiltering(filtering);
    pageResponse = repo.find(pageRequest);
    actualContent = pageResponse.getContent();
    assertThat(actualContent).hasSize(1);
    actual = actualContent.get(0);
    assertThat(actual.getEmail()).isEqualTo(user3.getEmail());
    assertThat(actual.getFirstname()).isEqualTo(user3.getFirstname());
    assertThat(actual.getLastname()).isEqualTo(user3.getLastname());
  }

  @Test
  @DisplayName("should return list of active admin users")
  public void getActiveAdminUsers() throws RepositoryException {
    User user1 = new User();
    user1.setEmail("homer@simpson.de");
    user1.setFirstname("Homer");
    user1.setLastname("Simpson");
    user1.setRoles(List.of(Role.ADMIN));
    repo.save(user1);

    List<User> admins = repo.getActiveAdminUsers();
    assertThat(admins.size()).isEqualTo(1);
  }
}
