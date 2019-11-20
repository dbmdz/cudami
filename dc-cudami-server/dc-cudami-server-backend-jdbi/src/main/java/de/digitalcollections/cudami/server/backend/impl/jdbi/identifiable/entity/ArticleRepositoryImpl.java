package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.ArticleRepository;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.Article;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.ArticleImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ArticleRepositoryImpl extends EntityRepositoryImpl<Article>
    implements ArticleRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(ArticleRepositoryImpl.class);

  @Autowired
  public ArticleRepositoryImpl(Jdbi dbi) {
    super(dbi);
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM articles";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public PageResponse<Article> find(PageRequest pageRequest) {
    StringBuilder query =
        new StringBuilder(
            "SELECT uuid, label, description, created, last_modified, text FROM articles");
    addPageRequestParams(pageRequest, query);

    List<ArticleImpl> result =
        dbi.withHandle(h -> h.createQuery(query.toString()).mapToBean(ArticleImpl.class).list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public Article findOne(UUID uuid) {
    String query =
        "SELECT uuid, label, description, created, last_modified, text FROM articles WHERE uuid = :uuid";
    Article article =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("uuid", uuid)
                    .mapToBean(ArticleImpl.class)
                    .findOne()
                    .orElse(null));
    return article;
  }

  @Override
  public Article findOne(Identifier identifier) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"uuid"};
  }

  @Override
  public Article save(Article article) {
    article.setUuid(UUID.randomUUID());
    article.setCreated(LocalDateTime.now());
    article.setLastModified(LocalDateTime.now());

    String query =
        "INSERT INTO articles("
            + "uuid, label, description, identifiable_type, entity_type, created, last_modified, text"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :type, :entityType, :created, :lastModified, :text::JSONB"
            + ") RETURNING *";
    Article result =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bindBean(article)
                    .mapToBean(ArticleImpl.class)
                    .findOne()
                    .orElse(null));
    return result;
  }

  @Override
  public Article update(Article article) {
    article.setLastModified(LocalDateTime.now());
    // do not update/left out from statement (not changed since insert): uuid, created,
    // identifiable_type, entity_type

    String query =
        "UPDATE articles SET"
            + " label=:label::JSONB, description=:description::JSONB, last_modified=:lastModified, text=:text::JSONB"
            + " WHERE uuid=:uuid"
            + " RETURNING *";
    Article result =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bindBean(article)
                    .mapToBean(ArticleImpl.class)
                    .findOne()
                    .orElse(null));
    return result;
  }
}
