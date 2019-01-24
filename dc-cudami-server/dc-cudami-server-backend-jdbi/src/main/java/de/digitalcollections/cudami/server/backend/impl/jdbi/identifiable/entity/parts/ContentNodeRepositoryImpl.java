package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.parts;

import de.digitalcollections.cudami.server.backend.api.repository.LocaleRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.parts.ContentNodeRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import de.digitalcollections.model.api.identifiable.parts.Translation;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifiableImpl;
import de.digitalcollections.model.impl.identifiable.entity.parts.ContentNodeImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class ContentNodeRepositoryImpl<C extends ContentNode, I extends Identifiable> extends IdentifiableRepositoryImpl<C> implements ContentNodeRepository<C, I> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContentNodeRepositoryImpl.class);

  private final IdentifiableRepository identifiableRepository;
  private final LocaleRepository localeRepository;

  @Autowired
  public ContentNodeRepositoryImpl(
    @Qualifier("identifiableRepositoryImpl") IdentifiableRepository identifiableRepository,
    LocaleRepository localeRepository,
    Jdbi dbi) {
    this.dbi = dbi;
    this.identifiableRepository = identifiableRepository;
    this.localeRepository = localeRepository;
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM contentnodes";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOnly());
    return count;
  }

  @Override
  public PageResponse<C> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder()
      .append("SELECT i.uuid as uuid, i.label as label, i.description as description")
      .append(" FROM contentnodes cn INNER JOIN identifiables i ON cn.uuid=i.uuid");

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
    StringBuilder query = new StringBuilder()
      .append("SELECT i.uuid as uuid, i.label as label, i.description as description")
      .append(" FROM contentnodes cn INNER JOIN identifiables i ON cn.uuid=i.uuid")
      .append(" WHERE cn.uuid = :uuid");

    List<ContentNodeImpl> list = dbi.withHandle(h -> h.createQuery(query.toString())
      .bind("uuid", uuid)
      .mapToBean(ContentNodeImpl.class)
      .list());
    if (list.isEmpty()) {
      return null;
    }
    C contentNode = (C) list.get(0);
    contentNode.setChildren(getChildren(contentNode));
    contentNode.setIdentifiables(getIdentifiables(contentNode));
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
    if (contentNode.getDescription() != null && contentNode.getDescription().getLocalizedStructuredContent() != null) {
      contentNode.getDescription().getLocalizedStructuredContent().entrySet().removeIf(entry -> !entry.getKey().equals(fLocale));
    }
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
    StringBuilder query = new StringBuilder()
      .append("SELECT i.uuid as uuid, i.label as label")
      .append(" FROM contentnodes cn INNER JOIN contentnode_contentnode cc ON cn.uuid=cc.parent_contentnode_uuid INNER JOIN identifiables i ON cc.child_contentnode_uuid=i.uuid")
      .append(" WHERE cn.uuid = :uuid")
      .append(" ORDER BY cc.sortIndex ASC");

    List<ContentNodeImpl> list = dbi.withHandle(h -> h.createQuery(query.toString())
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
    identifiableRepository.save(contentNode);

    dbi.withHandle(h -> h.createUpdate("INSERT INTO contentnodes(uuid) VALUES (:uuid)")
      .bindBean(contentNode)
      .execute());

    return findOne(contentNode.getUuid());
  }

  @Override
  public C saveWithParentContentTree(C contentNode, UUID parentContentTreeUuid) {
    identifiableRepository.save(contentNode);

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
    identifiableRepository.save(contentNode);

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
    identifiableRepository.update(contentNode);
    return findOne(contentNode.getUuid());
  }

  @Override
  public List<Identifiable> getIdentifiables(C contentNode) {
    return getIdentifiables(contentNode.getUuid());
  }

  @Override
  public List<Identifiable> getIdentifiables(UUID uuid) {
    // minimal data required for creating text links in a list
    String query = "SELECT i.uuid as uuid, i.label as label"
      + " FROM identifiables i INNER JOIN contentnode_identifiables ci ON ci.identifiable_uuid=i.uuid"
      + " WHERE ci.contentnode_uuid = :uuid"
      + " ORDER BY ci.sortIndex ASC";

    List<IdentifiableImpl> list = dbi.withHandle(h -> h.createQuery(query)
      .bind("uuid", uuid)
      .mapToBean(IdentifiableImpl.class)
      .list());

    if (list.isEmpty()) {
      return new ArrayList<>();
    }
    return list.stream().map(Identifiable.class::cast).collect(Collectors.toList());
  }

  @Override
  public void addIdentifiable(UUID identifiablesContainerUuid, UUID identifiableUuid) {
    Integer sortIndex = selectNextSortIndexForParentChildren(dbi, "contentnode_identifiables", "contentnode_uuid", identifiablesContainerUuid);
    dbi.withHandle(h -> h.createUpdate(
      "INSERT INTO contentnode_identifiables(contentnode_uuid, identifiable_uuid, sortIndex)"
      + " VALUES (:contentnode_uuid, :identifiable_uuid, :sortIndex)")
      .bind("contentnode_uuid", identifiablesContainerUuid)
      .bind("identifiable_uuid", identifiableUuid)
      .bind("sortIndex", sortIndex)
      .execute());
  }

  @Override
  public List<Identifiable> saveIdentifiables(C contentNode, List<Identifiable> identifiables) {
    UUID uuid = contentNode.getUuid();
    return saveIdentifiables(uuid, identifiables);
  }

  @Override
  public List<Identifiable> saveIdentifiables(UUID identifiablesContainerUuid, List<Identifiable> identifiables) {
    dbi.withHandle(h -> h.createUpdate("DELETE FROM contentnode_identifiables WHERE contentnode_uuid = :uuid")
      .bind("uuid", identifiablesContainerUuid).execute());

    PreparedBatch batch = dbi.withHandle(h -> h.prepareBatch("INSERT INTO contentnode_identifiables(contentnode_uuid, identifiable_uuid, sortIndex) VALUES(:uuid, :identifiableUuid, :sortIndex)"));
    for (Identifiable identifiable : identifiables) {
      batch.bind("uuid", identifiablesContainerUuid)
        .bind("identifiableUuid", identifiable.getUuid())
        .bind("sortIndex", identifiables.indexOf(identifiable))
        .add();
    }
    batch.execute();
    return getIdentifiables(identifiablesContainerUuid);
  }
}
