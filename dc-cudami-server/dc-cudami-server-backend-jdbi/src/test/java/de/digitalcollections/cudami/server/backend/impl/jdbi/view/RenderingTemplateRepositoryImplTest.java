package de.digitalcollections.cudami.server.backend.impl.jdbi.view;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendTestDatabase;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.validation.ValidationException;
import de.digitalcollections.model.view.RenderingTemplate;
import java.util.List;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    classes = RenderingTemplateRepositoryImpl.class)
@ContextConfiguration(classes = SpringConfigBackendTestDatabase.class)
@DisplayName("The RenderingTemplate Repository")
@Sql(scripts = "classpath:cleanup_database.sql")
public class RenderingTemplateRepositoryImplTest {

  RenderingTemplateRepositoryImpl repo;

  @Autowired Jdbi jdbi;

  @Autowired CudamiConfig cudamiConfig;

  @BeforeEach
  public void beforeEach() {
    repo = new RenderingTemplateRepositoryImpl(jdbi, cudamiConfig);
  }

  @Test
  @DisplayName("can find rendering templates")
  void find() throws RepositoryException, ValidationException {
    String name1 = "my-first-template";
    String name2 = "my-second-template";

    RenderingTemplate template1 = new RenderingTemplate();
    template1.setName(name1);
    repo.save(template1);

    RenderingTemplate template2 = new RenderingTemplate();
    template2.setName(name2);
    repo.save(template2);

    Filtering filtering =
        Filtering.builder()
            .add(FilterCriterion.builder().withExpression("name").contains(name1).build())
            .build();
    PageRequest pageRequest =
        PageRequest.builder().pageNumber(0).pageSize(99).filtering(filtering).build();
    PageResponse<RenderingTemplate> pageResponse = repo.find(pageRequest);
    List<RenderingTemplate> actualContent = pageResponse.getContent();
    assertThat(actualContent).hasSize(1);
    RenderingTemplate actual = actualContent.get(0);
    assertThat(actual.getName()).isEqualTo(template1.getName());
  }
}
