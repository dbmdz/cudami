package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.LocaleRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.ArticleRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.model.api.identifiable.entity.Article;
import de.digitalcollections.model.api.identifiable.parts.Translation;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.impl.PageResponseImpl;
import de.digitalcollections.model.impl.identifiable.entity.ArticleImpl;
import de.digitalcollections.model.impl.identifiable.parts.LocalizedTextImpl;
import de.digitalcollections.model.impl.identifiable.parts.structuredcontent.LocalizedStructuredContentImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
public class ArticleRepositoryImpl<A extends Article> extends EntityRepositoryImpl<A> implements ArticleRepository<A> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ArticleRepositoryImpl.class);

  private final EntityRepository entityRepository;
  private final LocaleRepository localeRepository;

  @Autowired
  public ArticleRepositoryImpl(
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
    String sql = "SELECT count(*) FROM articles";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOnly());
    return count;
  }

  @Override
  public A create() {
    Locale defaultLocale = localeRepository.getDefault();
    A article = (A) new ArticleImpl();
    article.setLabel(new LocalizedTextImpl(defaultLocale, ""));
    article.setDescription(new LocalizedStructuredContentImpl(defaultLocale));
    article.setText(new LocalizedStructuredContentImpl(defaultLocale));
    return article;
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

    List<ArticleImpl> list = dbi.withHandle(h -> h.createQuery(query)
            .bind("uuid", uuid)
            .mapToBean(ArticleImpl.class)
            .list());
    if (list.isEmpty()) {
      return null;
    }
    A article = (A) list.get(0);
    article.setChildren(getChildren(article));
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
    article.getDescription().getLocalizedStructuredContent().entrySet().removeIf(entry -> !entry.getKey().equals(fLocale));
    article.getText().getLocalizedStructuredContent().entrySet().removeIf((Map.Entry entry) -> !entry.getKey().equals(fLocale));
    return article;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[]{"uuid"};
  }

  @Override
  public List<A> getChildren(Article article) {
    return getChildren(article.getUuid());
  }

  @Override
  public List<A> getChildren(UUID uuid) {
    // minimal data required for creating text links in a list
    String query = "SELECT aa.child_article_uuid as uuid, i.label as label"
            + " FROM articles a INNER JOIN article_article aa ON a.uuid=aa.parent_article_uuid INNER JOIN identifiables i ON aa.child_article_uuid=i.uuid"
            + " WHERE a.uuid = :uuid"
            + " ORDER BY aa.sortIndex ASC";

    List<ArticleImpl> list = dbi.withHandle(h -> h.createQuery(query)
            .bind("uuid", uuid)
            .mapToBean(ArticleImpl.class)
            .list());

    if (list.isEmpty()) {
      return new ArrayList<>();
    }
    return list.stream().map(s -> (A) s).collect(Collectors.toList());
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
  public A saveWithParent(A article, UUID parentUuid) {
    entityRepository.save(article);

    dbi.withHandle(h -> h.createUpdate("INSERT INTO articles(uuid, text) VALUES (:uuid, :text::JSONB)")
            .bindBean(article)
            .execute());

    Integer sortIndex = selectNextSortIndexForParentChildren(dbi, "article_article", "parent_article_uuid", parentUuid);
    dbi.withHandle(h -> h.createUpdate(
            "INSERT INTO article_article(parent_article_uuid, child_article_uuid, sortIndex)"
            + " VALUES (:parent_uuid, :uuid, :sortIndex)")
            .bind("parent_uuid", parentUuid)
            .bind("sortIndex", sortIndex)
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
}
