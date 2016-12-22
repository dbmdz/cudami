package de.digitalcollections.cms.server.backend.impl.neo4j.repository;

import de.digitalcollections.cms.model.api.Category;
import de.digitalcollections.cms.model.api.Text;
import de.digitalcollections.cms.server.backend.impl.neo4j.model.CategoryImpl;
import de.digitalcollections.cms.server.backend.impl.neo4j.model.TextImpl;
import de.digitalcollections.cms.server.config.SpringConfigBackendNeo4jForTest;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpringConfigBackendNeo4jForTest.class})
@Transactional(readOnly = false)
public class CategoryRepositoryTest {

  @Autowired
  private CategoryRepository categoryRepository;

  @BeforeClass
  public static void beforeClass() {
    System.setProperty("spring.profiles.active", "TEST");
  }

  @Before
  public void before() throws Exception {
    // delete all
    categoryRepository.deleteAll();
  }

  @Test
  public void shouldCreateAndFindEntity() throws Exception {
    Text label = new TextImpl("test category");
    Category c = new CategoryImpl(label);
    CategoryImpl savedCategory = (CategoryImpl) categoryRepository.save(c);

    final UUID uuid = savedCategory.getUuid();
    final Long graphId = savedCategory.getGraphId();
    Assert.assertNotNull(graphId);

    Category foundCategory = categoryRepository.findOne(graphId);
    Assert.assertEquals(uuid, foundCategory.getUuid());
    Assert.assertEquals("test category", foundCategory.getLabel().getText());

  }
}
