package de.digitalcollections.cudami.server.backend.impl.jdbi.entity;

import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.core.model.impl.paging.PageResponseImpl;
import de.digitalcollections.cudami.model.api.entity.ContentTree;
import de.digitalcollections.cudami.model.api.enums.EntityType;
import de.digitalcollections.cudami.model.api.identifiable.ContentNode;
import de.digitalcollections.cudami.model.impl.entity.ContentTreeImpl;
import de.digitalcollections.cudami.server.backend.api.repository.entity.ContentTreeRepository;
import de.digitalcollections.cudami.server.backend.api.repository.entity.EntityRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractPagingAndSortingRepositoryImpl;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ContentTreeRepositoryImpl extends AbstractPagingAndSortingRepositoryImpl implements ContentTreeRepository<ContentTree> {

  @Autowired
  private Jdbi dbi;

  @Autowired
  private EntityRepository entityRepository;

  private final static String ENTITY_WHERE_CLAUSE = " entity_type='" + EntityType.CONTENT_TREE + "'";
  
  @Override
  public long count() {
    String sql = "SELECT count(*) FROM entities WHERE" + ENTITY_WHERE_CLAUSE;
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOnly());
    return count;
  }

  @Override
  public ContentTree create() {
    return new ContentTreeImpl();
  }

  @Override
  public PageResponse<ContentTree> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder("SELECT * FROM entities WHERE" + ENTITY_WHERE_CLAUSE);

    addPageRequestParams(pageRequest, query);
    List<ContentTreeImpl> result = dbi.withHandle(h -> h.createQuery(query.toString())
            .mapToBean(ContentTreeImpl.class)
            .list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public ContentTree findOne(UUID uuid) {
    List<ContentTreeImpl> list = dbi.withHandle(h -> h.createQuery(
            "SELECT * FROM entities WHERE" + ENTITY_WHERE_CLAUSE + "' AND uuid = :uuid")
            .bind("uuid", uuid)
            .mapToBean(ContentTreeImpl.class)
            .list());
    if (list.isEmpty()) {
      return null;
    }
    return list.get(0);
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return null;
  }

  @Override
  public List<ContentNode> getRootNodes(ContentTree contentTree) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public ContentTree save(ContentTree contentTree) {
    ContentTree result = (ContentTree) entityRepository.save(contentTree);
    saveRootNodes(contentTree);
    return result;
  }

  private void saveRootNodes(ContentTree contentTree) {
    List<ContentNode> rootNodes = contentTree.getRootNodes();
    // TODO: delete all persisted root nodes of this content tree
    if (rootNodes != null && !rootNodes.isEmpty()) {
      // TODO: save all root nodes of this content tree
    }
  }

  @Override
  public ContentTree update(ContentTree contentTree) {
    ContentTree result = (ContentTree) entityRepository.update(contentTree);
    saveRootNodes(contentTree);
    return result;
  }
}
