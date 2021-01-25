package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.WebsiteRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.parts.WebpageRepositoryImpl;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.WebsiteImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class WebsiteRepositoryImpl extends EntityRepositoryImpl<Website>
    implements WebsiteRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebsiteRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "ws";
  public static final String TABLE_ALIAS = "w";
  public static final String TABLE_NAME = "websites";

  public static String getSqlInsertFields() {
    return EntityRepositoryImpl.getSqlInsertFields() + ", registration_date, url";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  public static String getSqlInsertValues() {
    return EntityRepositoryImpl.getSqlInsertValues() + ", :registrationDate, :url";
  }

  public static String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  public static String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return EntityRepositoryImpl.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".url "
        + mappingPrefix
        + "_url, "
        + tableAlias
        + ".registration_date "
        + mappingPrefix
        + "_registrationDate";
  }

  public static String getSqlUpdateFieldValues() {
    return EntityRepositoryImpl.getSqlUpdateFieldValues()
        + ", registration_date=:registrationDate, url=:url";
  }

  private final WebpageRepositoryImpl webpageRepositoryImpl;

  @Autowired
  public WebsiteRepositoryImpl(
      Jdbi dbi,
      IdentifierRepository identifierRepository,
      WebpageRepositoryImpl webpageRepositoryImpl) {
    super(
        dbi,
        identifierRepository,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        WebsiteImpl.class,
        getSqlSelectAllFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlSelectReducedFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlInsertFields(),
        getSqlInsertValues(),
        getSqlUpdateFieldValues());
    this.webpageRepositoryImpl = webpageRepositoryImpl;
  }

  @Override
  public Website findOne(UUID uuid, Filtering filtering) {
    Website website = super.findOne(uuid, filtering);

    if (website != null) {
      website.setRootPages(getRootPages(website));
    }
    return website;
  }

  @Override
  public Website findOne(Identifier identifier) {
    Website website = super.findOne(identifier);

    if (website != null) {
      website.setRootPages(getRootPages(website));
    }
    return website;
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    List<String> allowedOrderByFields = super.getAllowedOrderByFields();
    allowedOrderByFields.addAll(Arrays.asList("url"));
    return allowedOrderByFields;
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    if (super.getColumnName(modelProperty) != null) {
      return super.getColumnName(modelProperty);
    }
    switch (modelProperty) {
      case "url":
        return tableAlias + ".url";
      default:
        return null;
    }
  }

  @Override
  public List<Locale> getLanguages() {
    String query =
        "SELECT DISTINCT languages FROM websites as w, jsonb_object_keys(w.label) as languages";
    List<Locale> result = dbi.withHandle(h -> h.createQuery(query).mapTo(Locale.class).list());
    return result;
  }

  @Override
  public List<Webpage> getRootPages(UUID uuid) {
    final String wpTableAlias = webpageRepositoryImpl.getTableAlias();
    final String wpTableName = webpageRepositoryImpl.getTableName();

    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM "
                + wpTableName
                + " AS "
                + wpTableAlias
                + " INNER JOIN website_webpages ww ON "
                + wpTableAlias
                + ".uuid = ww.webpage_uuid"
                + " WHERE ww.website_uuid = :uuid");

    List<Webpage> result =
        webpageRepositoryImpl.retrieveList(
            webpageRepositoryImpl.getSqlSelectReducedFields(), innerQuery, Map.of("uuid", uuid));
    return result;
  }

  @Override
  public PageResponse<Webpage> getRootPages(UUID uuid, PageRequest pageRequest) {
    final String wpTableAlias = webpageRepositoryImpl.getTableAlias();
    final String wpTableName = webpageRepositoryImpl.getTableName();

    String commonSql =
        " FROM "
            + wpTableName
            + " AS "
            + wpTableAlias
            + " INNER JOIN website_webpages ww ON "
            + wpTableAlias
            + ".uuid = ww.webpage_uuid"
            + " WHERE ww.website_uuid = :uuid";

    StringBuilder innerQuery = new StringBuilder("SELECT *" + commonSql);
    addFiltering(pageRequest, innerQuery);
    if (pageRequest.getSorting() == null) {
      innerQuery.append(" ORDER BY ww.sortIndex ASC");
    }
    addPageRequestParams(pageRequest, innerQuery);

    List<Webpage> result =
        webpageRepositoryImpl.retrieveList(
            webpageRepositoryImpl.getSqlSelectReducedFields(), innerQuery, Map.of("uuid", uuid));

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    addFiltering(pageRequest, countQuery);
    long total = retrieveCount(countQuery, Map.of("uuid", uuid));

    return new PageResponseImpl<>(result, pageRequest, total);
  }

  @Override
  public Website save(Website website) {
    super.save(website);
    Website result = findOne(website.getUuid());
    return result;
  }

  @Override
  public Website update(Website website) {
    super.update(website);
    Website result = findOne(website.getUuid());
    return result;
  }

  @Override
  public boolean updateRootPagesOrder(UUID websiteUuid, List<Webpage> rootPages) {
    if (websiteUuid == null || rootPages == null) {
      return false;
    }
    String query =
        "UPDATE website_webpages"
            + " SET sortindex = :idx"
            + " WHERE website_uuid = :websiteUuid AND webpage_uuid = :webpageUuid;";
    dbi.withHandle(
        h -> {
          PreparedBatch batch = h.prepareBatch(query);
          int idx = 0;
          for (Webpage webpage : rootPages) {
            batch
                .bind("idx", idx++)
                .bind("webpageUuid", webpage.getUuid())
                .bind("websiteUuid", websiteUuid)
                .add();
          }
          return batch.execute();
        });
    return true;
  }
}
