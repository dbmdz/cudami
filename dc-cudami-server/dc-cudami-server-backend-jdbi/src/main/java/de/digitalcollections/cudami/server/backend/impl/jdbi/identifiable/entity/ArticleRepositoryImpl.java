package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.ArticleRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.entity.Article;
import de.digitalcollections.model.api.identifiable.parts.Translation;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifiableImpl;
import de.digitalcollections.model.impl.identifiable.entity.ArticleImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
public class ArticleRepositoryImpl<A extends Article, I extends Identifiable> extends EntityRepositoryImpl<A> implements ArticleRepository<A, I> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ArticleRepositoryImpl.class);

  private final EntityRepository entityRepository;

  @Autowired
  public ArticleRepositoryImpl(
      @Qualifier("identifiableRepositoryImpl") IdentifiableRepository identifiableRepository,
      @Qualifier("entityRepositoryImpl") EntityRepository entityRepository,
      Jdbi dbi) {
    super(dbi, identifiableRepository);
    this.entityRepository = entityRepository;
  }

  @Override
  public void addIdentifiable(UUID articleUuid, UUID identifiableUuid) {
    Integer sortIndex = selectNextSortIndexForParentChildren(dbi, "article_identifiables", "article_uuid", articleUuid);
    dbi.withHandle(h -> h.createUpdate(
        "INSERT INTO article_identifiables(article_uuid, identifiable_uuid, sortIndex)"
        + " VALUES (:article_uuid, :identifiable_uuid, :sortIndex)")
        .bind("article_uuid", articleUuid)
        .bind("identifiable_uuid", identifiableUuid)
        .bind("sortIndex", sortIndex)
        .execute());
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM articles";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOnly());
    return count;
  }

  @Override
  public PageResponse<A> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder("SELECT a.uuid as uuid, a.text as text, i.label as label, i.description as description")
        .append(" FROM articles a INNER JOIN entities e ON a.uuid=e.uuid INNER JOIN identifiables i ON a.uuid=i.uuid");

    addPageRequestParams(pageRequest, query);

//    List<Map<String, Object>> list = dbi.withHandle(h -> h.createQuery(query.toString()).mapToMap().list());
    List<ArticleImpl> result = dbi.withHandle(h -> h.createQuery(query.toString())
        .mapToBean(ArticleImpl.class)
        .list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
//    PageResponse pageResponse = new PageResponseImpl(null, pageRequest, total);
    return pageResponse;
  }

  @Override
  public A findOne(UUID uuid) {
    String query = "SELECT a.uuid as uuid, a.text as text, i.label as label, i.description as description"
                   + " FROM articles a INNER JOIN entities e ON a.uuid=e.uuid INNER JOIN identifiables i ON a.uuid=i.uuid"
                   + " WHERE a.uuid = :uuid";

    A article = (A) dbi.withHandle(h -> h.createQuery(query)
        .bind("uuid", uuid)
        .mapToBean(ArticleImpl.class)
        .findOnly());
    if (article != null) {
      article.setIdentifiables(getIdentifiables(article));
    }
    return article;
  }

  @Override
  public A findOne(UUID uuid, Locale locale) {
    A article = findOne(uuid);
    Set<Translation> translations = article.getLabel().getTranslations();

    if (locale == null) {
      // just return first existing locale
      Optional<Translation> translation = translations.stream().findFirst();
      locale = translation.map(Translation::getLocale).orElse(null);
    }
    final Locale fLocale = locale;
    if (fLocale == null) {
      // a webpage/identifiable without label does not make sense...
      return null;
    }

    // if requested locale does not exist, return null
    boolean requestedTranslationExists = translations.stream().anyMatch(translation -> translation.getLocale().equals(fLocale));
    if (!requestedTranslationExists) {
      return null;
    }

    // TODO maybe a better solution to just get locale specific fields directly from database instead of removing it here?
    // iterate over all localized fields and remove all texts that are not matching the requested locale:
    article.getLabel().getTranslations().removeIf(translation -> !translation.getLocale().equals(fLocale));
    if (article.getDescription() != null && article.getDescription().getLocalizedStructuredContent() != null) {
      article.getDescription().getLocalizedStructuredContent().entrySet().removeIf(entry -> !entry.getKey().equals(fLocale));
    }
    if (article.getText() != null && article.getText().getLocalizedStructuredContent() != null) {
      article.getText().getLocalizedStructuredContent().entrySet().removeIf((Map.Entry entry) -> !entry.getKey().equals(fLocale));
    }
    return article;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[]{"uuid"};
  }

  @Override
  public A save(A article) {
    entityRepository.save(article);

    dbi.withHandle(h -> h.createUpdate("INSERT INTO articles(uuid, text) VALUES (:uuid, :text::JSONB)")
        .bindBean(article)
        .execute());

    return findOne(article.getUuid());
  }

  @Override
  public A update(A article) {
    entityRepository.update(article);
    dbi.withHandle(h -> h.createUpdate("UPDATE articles SET text=:text::JSONB WHERE uuid=:uuid")
        .bindBean(article)
        .execute());
    return findOne(article.getUuid());
  }

  @Override
  public List<Identifiable> getIdentifiables(A article) {
    return getIdentifiables(article.getUuid());
  }

  @Override
  public List<Identifiable> getIdentifiables(UUID identifiableUuid) {
    // minimal data required for creating text links in a list
    String query = "SELECT i.uuid as uuid, i.label as label"
                   + " FROM identifiables i INNER JOIN article_identifiables ai ON ai.identifiable_uuid=i.uuid"
                   + " WHERE ai.article_uuid = :uuid"
                   + " ORDER BY ai.sortIndex ASC";

    List<IdentifiableImpl> list = dbi.withHandle(h -> h.createQuery(query)
        .bind("uuid", identifiableUuid)
        .mapToBean(IdentifiableImpl.class)
        .list());

    if (list.isEmpty()) {
      return new ArrayList<>();
    }
    return list.stream().map(Identifiable.class::cast).collect(Collectors.toList());
  }

  @Override
  public List<Identifiable> saveIdentifiables(A article, List<Identifiable> identifiables) {
    UUID uuid = article.getUuid();
    return saveIdentifiables(uuid, identifiables);
  }

  @Override
  public List<Identifiable> saveIdentifiables(UUID identifiablesContainerUuid, List<Identifiable> identifiables) {
    dbi.withHandle(h -> h.createUpdate("DELETE FROM article_identifiables WHERE article_uuid = :uuid")
        .bind("uuid", identifiablesContainerUuid).execute());

    PreparedBatch batch = dbi.withHandle(h -> h.prepareBatch("INSERT INTO article_identifiables(article_uuid, identifiable_uuid, sortIndex) VALUES(:uuid, :identifiableUuid, :sortIndex)"));
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
