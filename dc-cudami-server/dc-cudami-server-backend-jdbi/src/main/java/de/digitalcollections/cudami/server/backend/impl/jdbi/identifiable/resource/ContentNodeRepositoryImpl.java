package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.core.model.impl.paging.PageResponseImpl;
import de.digitalcollections.cudami.model.api.identifiable.parts.Translation;
import de.digitalcollections.cudami.model.api.identifiable.resource.ContentNode;
import de.digitalcollections.cudami.model.impl.identifiable.parts.MultilanguageDocumentImpl;
import de.digitalcollections.cudami.model.impl.identifiable.parts.TextImpl;
import de.digitalcollections.cudami.model.impl.identifiable.resource.ContentNodeImpl;
import de.digitalcollections.cudami.server.backend.api.repository.LocaleRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.ContentNodeRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.ResourceRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractPagingAndSortingRepositoryImpl;
import java.util.*;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ContentNodeRepositoryImpl extends AbstractPagingAndSortingRepositoryImpl implements ContentNodeRepository<ContentNode> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContentNodeRepositoryImpl.class);

  @Autowired
  private Jdbi dbi;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  private ResourceRepository resourceRepository;

  @Autowired
  LocaleRepository localeRepository;

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM contentnodes";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOnly());
    return count;
  }

  @Override
  public ContentNode create() {
    Locale defaultLocale = localeRepository.getDefault();
    ContentNodeImpl contentNode = new ContentNodeImpl();
    contentNode.setLabel(new TextImpl(defaultLocale, ""));
    contentNode.setDescription(new MultilanguageDocumentImpl(defaultLocale));
    return contentNode;
  }

  @Override
  public PageResponse<ContentNode> find(PageRequest pageRequest) {
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
  public ContentNode findOne(UUID uuid) {
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
    ContentNodeImpl contentNode = list.get(0);
    contentNode.setSubNodes(getSubNodes(contentNode));
    return contentNode;
  }

  @Override
  public ContentNode findOne(UUID uuid, Locale locale) {
    ContentNode contentNode = findOne(uuid);
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
    contentNode.getDescription().getDocuments().entrySet().removeIf(entry -> !entry.getKey().equals(fLocale));
    return contentNode;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[]{"uuid"};
  }

  @Override
  public List<ContentNode> getSubNodes(ContentNode contentNode) {
    // minimal data required for creating text links in a list
    String query = "SELECT cc.child_contentnode_uuid as uuid, i.label as label"
            + " FROM contentnodes cn INNER JOIN contentnode_contentnode cc ON cn.uuid=cc.parent_contentnode_uuid INNER JOIN identifiables i ON cc.child_contentnode_uuid=i.uuid"
            + " WHERE cn.uuid = :uuid";

    List<ContentNodeImpl> list = dbi.withHandle(h -> h.createQuery(query)
            .bind("uuid", contentNode.getUuid())
            .mapToBean(ContentNodeImpl.class)
            .list());

    if (list.isEmpty()) {
      return new ArrayList<>();
    }
    return list.stream().map(ContentNode.class::cast).collect(Collectors.toList());
  }

  @Override
  public ContentNode save(ContentNode contentNode) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public ContentNode saveWithParentContentTree(ContentNode contentNode, UUID parentContentTreeUuid) {
    resourceRepository.save(contentNode);

    dbi.withHandle(h -> h.createUpdate("INSERT INTO contentnodes(uuid) VALUES (:uuid)")
            .bindBean(contentNode)
            .execute());

    dbi.withHandle(h -> h.createUpdate("INSERT INTO contenttree_contentnode(contenttree_uuid, contentnode_uuid) VALUES (:parent_contenttree_uuid, :uuid)")
            .bind("parent_contenttree_uuid", parentContentTreeUuid)
            .bindBean(contentNode)
            .execute());

    return findOne(contentNode.getUuid());
  }

  @Override
  public ContentNode saveWithParentContentNode(ContentNode contentNode, UUID parentContentNodeUuid) {
    resourceRepository.save(contentNode);

    dbi.withHandle(h -> h.createUpdate("INSERT INTO contentnodes(uuid) VALUES (:uuid)")
            .bindBean(contentNode)
            .execute());

    dbi.withHandle(h -> h.createUpdate("INSERT INTO contentnode_contentnode(parent_contentnode_uuid, child_contentnode_uuid) VALUES (:parent_contentnode_uuid, :uuid)")
            .bind("parent_contentnode_uuid", parentContentNodeUuid)
            .bindBean(contentNode)
            .execute());

    return findOne(contentNode.getUuid());
  }

  @Override
  public ContentNode update(ContentNode contentNode) {
    resourceRepository.update(contentNode);
    return findOne(contentNode.getUuid());
  }
}
