package de.digitalcollections.cudami.server.backend.impl.neo4j.repository;

import de.digitalcollections.cudami.server.backend.impl.neo4j.repository.CategoryRepository;
import de.digitalcollections.cudami.model.api.Text;
import de.digitalcollections.cudami.server.backend.impl.neo4j.model.TextImpl;
import de.digitalcollections.cudami.server.backend.impl.neo4j.model.entity.CategoryImpl;
import de.digitalcollections.cudami.server.config.SpringConfigBackendNeo4jForTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import de.digitalcollections.cudami.model.api.entity.ContentNode;

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
  public void shouldCreateAndFindByGraphidEntity() throws Exception {
    Text label = new TextImpl("test category");
    ContentNode c = new CategoryImpl(label);
    CategoryImpl savedCategory = (CategoryImpl) categoryRepository.save(c);

    final String uuid = savedCategory.getUuid();
    final Long graphId = savedCategory.getId();
    Assert.assertNotNull(graphId);

    ContentNode foundCategory = categoryRepository.findOne(graphId);
    Assert.assertEquals(uuid, foundCategory.getUuid());
    Assert.assertEquals("test category", foundCategory.getLabel().getText());
  }

  @Test
  public void shouldCreateAndFindByUuidEntity() throws Exception {
    Text label = new TextImpl("test category");
    ContentNode c = new CategoryImpl(label);
    CategoryImpl savedCategory = (CategoryImpl) categoryRepository.save(c);

    final String uuid = savedCategory.getUuid();
    final Long graphId = savedCategory.getId();
    Assert.assertNotNull(graphId);

    ContentNode foundCategory = categoryRepository.findByUuid(uuid);
    Assert.assertEquals(uuid, foundCategory.getUuid());
    Assert.assertEquals("test category", foundCategory.getLabel().getText());
  }

  @Test
  public void shouldCreateAndDeleteEntity() throws Exception {
    Text label = new TextImpl("test category");
    ContentNode c = new CategoryImpl(label);
    CategoryImpl savedCategory = (CategoryImpl) categoryRepository.save(c);

    final Long graphId = savedCategory.getId();
    Assert.assertNotNull(graphId);

    categoryRepository.delete(graphId);

    ContentNode foundCategory = categoryRepository.findOne(graphId);
    Assert.assertNull(foundCategory);
  }
}
