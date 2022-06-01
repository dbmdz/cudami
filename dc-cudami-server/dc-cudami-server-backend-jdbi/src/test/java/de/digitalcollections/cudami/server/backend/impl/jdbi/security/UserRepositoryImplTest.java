package de.digitalcollections.cudami.server.backend.impl.jdbi.security;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendDatabase;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.security.User;
import java.util.List;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    classes = UserRepositoryImpl.class)
@ContextConfiguration(classes = SpringConfigBackendDatabase.class)
@DisplayName("The User Repository")
public class UserRepositoryImplTest {

  UserRepositoryImpl repo;

  @Autowired PostgreSQLContainer postgreSQLContainer;

  @Autowired Jdbi jdbi;

  @Autowired CudamiConfig cudamiConfig;

  @BeforeEach
  public void beforeEach() {
    repo = new UserRepositoryImpl(jdbi, cudamiConfig);
  }

  @Test
  @DisplayName("can find rendering templates")
  void find() {
    User user1 = new User();
    user1.setEmail("homer@simpson.de");
    user1.setFirstname("Homer");
    user1.setLastname("Simpson");
    user1 = repo.save(user1);

    User user2 = new User();
    user2.setEmail("marge@simpson.de");
    user2.setFirstname("Marjorie");
    user2.setLastname("Simpson");
    user2 = repo.save(user2);

    User user3 = new User();
    user3.setEmail("wiggum@police-springfield.gov");
    user3.setFirstname("Clancy");
    user3.setLastname("Wiggum");
    user3 = repo.save(user3);

    PageRequest pageRequest =
        PageRequest.builder().pageNumber(0).pageSize(99).searchTerm("simpson.de").build();
    PageResponse<User> pageResponse = repo.find(pageRequest);
    List<User> actualContent = pageResponse.getContent();
    assertThat(actualContent).hasSize(2);
    User actual = actualContent.get(0);
    assertThat(actual.getEmail()).isEqualTo(user1.getEmail());
    assertThat(actual.getFirstname()).isEqualTo(user1.getFirstname());
    assertThat(actual.getLastname()).isEqualTo(user1.getLastname());

    pageRequest = PageRequest.builder().pageNumber(0).pageSize(99).searchTerm("marjorie").build();
    pageResponse = repo.find(pageRequest);
    actualContent = pageResponse.getContent();
    assertThat(actualContent).hasSize(1);
    actual = actualContent.get(0);
    assertThat(actual.getEmail()).isEqualTo(user2.getEmail());
    assertThat(actual.getFirstname()).isEqualTo(user2.getFirstname());
    assertThat(actual.getLastname()).isEqualTo(user2.getLastname());

    pageRequest = PageRequest.builder().pageNumber(0).pageSize(99).searchTerm("Wiggum").build();
    pageResponse = repo.find(pageRequest);
    actualContent = pageResponse.getContent();
    assertThat(actualContent).hasSize(1);
    actual = actualContent.get(0);
    assertThat(actual.getEmail()).isEqualTo(user3.getEmail());
    assertThat(actual.getFirstname()).isEqualTo(user3.getFirstname());
    assertThat(actual.getLastname()).isEqualTo(user3.getLastname());
  }
}
