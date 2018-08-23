package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.LocaleRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.ContentNodeRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.ResourceRepository;
import de.digitalcollections.model.api.identifiable.parts.Translation;
import de.digitalcollections.model.api.identifiable.resource.ContentNode;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.impl.PageResponseImpl;
import de.digitalcollections.model.impl.identifiable.parts.LocalizedTextImpl;
import de.digitalcollections.model.impl.identifiable.parts.structuredcontent.LocalizedStructuredContentImpl;
import de.digitalcollections.model.impl.identifiable.resource.ContentNodeImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class ContentNodeRepositoryImpl<C extends ContentNode> extends ResourceRepositoryImpl<C> implements ContentNodeRepository<C> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContentNodeRepositoryImpl.class);

  private final ResourceRepository resourceRepository;
  private final LocaleRepository localeRepository;

  @Autowired
  public ContentNodeRepositoryImpl(
          @Qualifier("identifiableRepositoryImpl") IdentifiableRepository identifiableRepository,
          @Qualifier("resourceRepositoryImpl") ResourceRepository resourceRepository,
          LocaleRepository localeRepository,
          Jdbi dbi) {
    super(dbi, identifiableRepository);
    this.resourceRepository = resourceRepository;
    this.localeRepository = localeRepository;
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM contentnodes";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOnly());
    return count;
  }

  @Override
  public C create() {
    Locale defaultLocale = localeRepository.getDefault();
    C contentNode = (C) new ContentNodeImpl();
    contentNode.setLabel(new LocalizedTextImpl(defaultLocale, ""));
    contentNode.setDescription(new LocalizedStructuredContentImpl(defaultLocale));
    return contentNode;
  }

  @Override
  public PageResponse<C> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder("SELECT cn.uuid as uuid, i.label as label, i.description as description")
            .append(" FROM contentnodes cn INNER JOIN resources r ON cn.uuid=r.uuid INNER JOIN identifiables i ON cn.uuid=i.uuid");

    addPageRequestParams(pageRequest, query);

//    List<Map<String, Object>> list = dbi.withHandle(h -> h.createQuery(query.toString()).mapToMap().list());
    List<ContentNodeImpl> result = dbi.withHandle(h -> h.createQuery(query.toString())
            .mapToBean(ContentNodeImpl.class)
            .list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
//    PageResponse pageResponse = new PageResponseImpl(null, pageRequest, total);
    return pageResponse;
  }

  @Override
  public C findOne(UUID uuid) {
    String query = "SELECT cn.uuid as uuid, i.label as label, i.description as description"
            + " FROM contentnodes cn INNER JOIN resources r ON cn.uuid=r.uuid INNER JOIN identifiables i ON cn.uuid=i.uuid"
            + " WHERE cn.uuid = :uuid";

    List<ContentNodeImpl> list = dbi.withHandle(h -> h.createQuery(query)
            .bind("uuid", uuid)
            .mapToBean(ContentNodeImpl.class)
            .list());
    if (list.isEmpty()) {
      return null;
    }
    C contentNode = (C) list.get(0);
    contentNode.setChildren(getChildren(contentNode));
    return contentNode;
  }

  @Override
  public C findOne(UUID uuid, Locale locale) {
    C contentNode = findOne(uuid);
    Set<Translation> translations = contentNode.getLabel().getTranslations();

    if (locale == null) {
      // just return first existing locale
      Optional<Translation> translation = translations.stream().findFirst();
      locale = translation.map(Translation::getLocale).orElse(null);
    }
    final Locale fLocale = locale;
    if (fLocale == null) {
      // a contentnode/identifiable without label does not make sense...
      return null;
    }

    // if requested locale does not exist, return null
    boolean requestedTranslationExists = translations.stream().anyMatch(translation -> translation.getLocale().equals(fLocale));
    if (!requestedTranslationExists) {
      return null;
    }

    // TODO maybe a better solution to just get locale specific fields directly from database instead of removing it here?
    // iterate over all localized fields and remove all texts that are not matching the requested locale:
    contentNode.getLabel().getTranslations().removeIf(translation -> !translation.getLocale().equals(fLocale));
    contentNode.getDescription().getLocalizedStructuredContent().entrySet().removeIf(entry -> !entry.getKey().equals(fLocale));
    return contentNode;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[]{"uuid"};
  }

  @Override
  public List<C> getChildren(C contentNode) {
    return getChildren(contentNode.getUuid());
  }

  @Override
  public List<C> getChildren(UUID uuid) {
    // minimal data required for creating text links in a list
    String query = "SELECT cc.child_contentnode_uuid as uuid, i.label as label"
            + " FROM contentnodes cn INNER JOIN contentnode_contentnode cc ON cn.uuid=cc.parent_contentnode_uuid INNER JOIN identifiables i ON cc.child_contentnode_uuid=i.uuid"
            + " WHERE cn.uuid = :uuid"
            + " ORDER BY cc.sortIndex ASC";

    List<ContentNodeImpl> list = dbi.withHandle(h -> h.createQuery(query)
            .bind("uuid", uuid)
            .mapToBean(ContentNodeImpl.class)
            .list());

    if (list.isEmpty()) {
      return new ArrayList<>();
    }
    return list.stream().map(s -> (C) s).collect(Collectors.toList());
  }

  @Override
  public C save(C contentNode) {
    resourceRepository.save(contentNode);

    dbi.withHandle(h -> h.createUpdate("INSERT INTO contentnodes(uuid) VALUES (:uuid)")
            .bindBean(contentNode)
            .execute());

    return findOne(contentNode.getUuid());
  }

  @Override
  public C saveWithParentContentTree(C contentNode, UUID parentContentTreeUuid) {
    resourceRepository.save(contentNode);

    dbi.withHandle(h -> h.createUpdate("INSERT INTO contentnodes(uuid) VALUES (:uuid)")
            .bindBean(contentNode)
            .execute());

    Integer sortIndex = selectNextSortIndexForParentChildren(dbi, "contenttree_contentnode", "contenttree_uuid", parentContentTreeUuid);
    dbi.withHandle(h -> h.createUpdate(
            "INSERT INTO contenttree_contentnode(contenttree_uuid, contentnode_uuid, sortIndex)"
            + " VALUES (:parent_contenttree_uuid, :uuid, :sortIndex)")
            .bind("parent_contenttree_uuid", parentContentTreeUuid)
            .bind("sortIndex", sortIndex)
            .bindBean(contentNode)
            .execute());

    return findOne(contentNode.getUuid());
  }

  @Override
  public C saveWithParentContentNode(C contentNode, UUID parentContentNodeUuid) {
    resourceRepository.save(contentNode);

    dbi.withHandle(h -> h.createUpdate("INSERT INTO contentnodes(uuid) VALUES (:uuid)")
            .bindBean(contentNode)
            .execute());

    Integer sortIndex = selectNextSortIndexForParentChildren(dbi, "contentnode_contentnode", "parent_contentnode_uuid", parentContentNodeUuid);
    dbi.withHandle(h -> h.createUpdate(
            "INSERT INTO contentnode_contentnode(parent_contentnode_uuid, child_contentnode_uuid, sortIndex)"
            + " VALUES (:parent_contentnode_uuid, :uuid, :sortIndex)")
            .bind("parent_contentnode_uuid", parentContentNodeUuid)
            .bind("sortIndex", sortIndex)
            .bindBean(contentNode)
            .execute());

    return findOne(contentNode.getUuid());
  }

  @Override
  public C update(C contentNode) {
    resourceRepository.update(contentNode);
    return findOne(contentNode.getUuid());
  }
}
