package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.LocaleRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.ContentTreeRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.model.api.identifiable.entity.ContentTree;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.impl.PageResponseImpl;
import de.digitalcollections.model.impl.identifiable.entity.ContentTreeImpl;
import de.digitalcollections.model.impl.identifiable.entity.parts.ContentNodeImpl;
import de.digitalcollections.model.impl.identifiable.parts.LocalizedTextImpl;
import de.digitalcollections.model.impl.identifiable.parts.structuredcontent.LocalizedStructuredContentImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class ContentTreeRepositoryImpl<C extends ContentTree> extends EntityRepositoryImpl<C> implements ContentTreeRepository<C> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContentTreeRepositoryImpl.class);

  private final EntityRepository entityRepository;
  private final LocaleRepository localeRepository;

  @Autowired
  public ContentTreeRepositoryImpl(
          @Qualifier("identifiableRepositoryImpl") IdentifiableRepository identifiableRepository,
          @Qualifier("entityRepositoryImpl") EntityRepository entityRepository,
          LocaleRepository localeRepository,
          Jdbi dbi) {
    super(dbi, identifiableRepository);
    this.entityRepository = entityRepository;
    this.localeRepository = localeRepository;
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM contenttrees";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOnly());
    return count;
  }

  @Override
  public C create() {
    Locale defaultLocale = localeRepository.getDefault();
    C contentTree = (C) new ContentTreeImpl();
    contentTree.setLabel(new LocalizedTextImpl(defaultLocale, ""));
    contentTree.setDescription(new LocalizedStructuredContentImpl(defaultLocale));
    return contentTree;
  }

  @Override
  public PageResponse<C> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder("SELECT ct.id as id, ct.uuid as uuid, i.label as label, i.description as description")
            .append(" FROM contenttrees ct INNER JOIN entities e ON ct.uuid=e.uuid INNER JOIN identifiables i ON ct.uuid=i.uuid");

    addPageRequestParams(pageRequest, query);

    List<ContentTreeImpl> result = dbi.withHandle(h -> h.createQuery(query.toString())
            .mapToBean(ContentTreeImpl.class)
            .list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public C findOne(UUID uuid) {
    String query = "SELECT ct.uuid as uuid, i.label as label, i.description as description"
            + " FROM contenttrees ct INNER JOIN entities e ON ct.uuid=e.uuid INNER JOIN identifiables i ON ct.uuid=i.uuid"
            + " WHERE ct.uuid = :uuid";

    List<ContentTreeImpl> list = dbi.withHandle(h -> h.createQuery(query)
            .bind("uuid", uuid)
            .mapToBean(ContentTreeImpl.class)
            .list());
    if (list.isEmpty()) {
      return null;
    }
    C contentTree = (C) list.get(0);
    contentTree.setRootNodes(getRootNodes(contentTree));
    return contentTree;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return null;
  }

  @Override
  public C save(C contentTree) {
    entityRepository.save(contentTree);
    dbi.withHandle(h -> h.createUpdate("INSERT INTO contenttrees(uuid) VALUES (:uuid)")
            .bindBean(contentTree)
            .execute());
    return findOne(contentTree.getUuid());
  }

  @Override
  public C update(C contentTree) {
    entityRepository.update(contentTree);
    return findOne(contentTree.getUuid());
  }

  @Override
  public List<ContentNode> getRootNodes(C contentTree) {
    UUID uuid = contentTree.getUuid();
    return getRootNodes(uuid);
  }

  @Override
  public List<ContentNode> getRootNodes(UUID uuid) {
    // minimal data required for creating text links in a list
    String query = "SELECT cc.contentnode_uuid as uuid, i.label as label"
            + " FROM contenttrees ct INNER JOIN contenttree_contentnode cc ON ct.uuid=cc.contenttree_uuid INNER JOIN identifiables i ON cc.contentnode_uuid=i.uuid"
            + " WHERE ct.uuid = :uuid"
            + " ORDER BY cc.sortIndex ASC";

    List<ContentNodeImpl> list = dbi.withHandle(h -> h.createQuery(query)
            .bind("uuid", uuid)
            .mapToBean(ContentNodeImpl.class)
            .list());

    if (list.isEmpty()) {
      return new ArrayList<>();
    }
    return list.stream().map(ContentNode.class::cast).collect(Collectors.toList());
  }
}
