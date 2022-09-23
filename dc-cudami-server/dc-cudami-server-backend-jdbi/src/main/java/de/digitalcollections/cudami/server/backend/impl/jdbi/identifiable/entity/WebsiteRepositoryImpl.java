package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.WebsiteRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.SearchTermTemplates;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.web.WebpageRepositoryImpl;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.list.filtering.Filtering;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

  @Override
  public String getSqlInsertFields() {
    return super.getSqlInsertFields() + ", registration_date, url";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  @Override
  public String getSqlInsertValues() {
    return super.getSqlInsertValues() + ", :registrationDate, :url";
  }

  @Override
  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
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

  @Override
  public String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues() + ", registration_date=:registrationDate, url=:url";
  }

  private final WebpageRepositoryImpl webpageRepositoryImpl;

  @Autowired
  public WebsiteRepositoryImpl(
      Jdbi dbi, WebpageRepositoryImpl webpageRepositoryImpl, CudamiConfig cudamiConfig) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Website.class,
        cudamiConfig.getOffsetForAlternativePaging());
    this.webpageRepositoryImpl = webpageRepositoryImpl;
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    List<String> allowedOrderByFields = super.getAllowedOrderByFields();
    allowedOrderByFields.addAll(Arrays.asList("url"));
    return allowedOrderByFields;
  }

  @Override
  public Website getByIdentifier(Identifier identifier) {
    Website website = super.getByIdentifier(identifier);

    if (website != null) {
      website.setRootPages(getRootPages(website));
    }
    return website;
  }

  @Override
  public Website getByUuidAndFiltering(UUID uuid, Filtering filtering) {
    Website website = super.getByUuidAndFiltering(uuid, filtering);

    if (website != null) {
      website.setRootPages(getRootPages(website));
    }
    return website;
  }

  @Override
  public String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    if (super.getColumnName(modelProperty) != null) {
      return super.getColumnName(modelProperty);
    }
    switch (modelProperty) {
      case "url":
        return tableAlias + ".url";
      case "registrationDate":
        return tableAlias + ".registration_date";
      default:
        return null;
    }
  }

  @Override
  public List<Webpage> getRootWebpages(UUID uuid) {
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
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", uuid);

    List<Webpage> result =
        webpageRepositoryImpl.retrieveList(
            webpageRepositoryImpl.getSqlSelectReducedFields(), innerQuery, argumentMappings, null);
    return result;
  }

  @Override
  protected List<String> getSearchTermTemplates(String tblAlias, String originalSearchTerm) {
    if (originalSearchTerm == null) {
      return Collections.EMPTY_LIST;
    }
    List<String> searchTermTemplates = super.getSearchTermTemplates(tblAlias, originalSearchTerm);
    searchTermTemplates.add(SearchTermTemplates.ILIKE_SEARCH.renderTemplate(tblAlias, "url"));
    return searchTermTemplates;
  }

  @Override
  public Website save(Website website) {
    super.save(website);
    Website result = getByUuid(website.getUuid());
    return result;
  }

  @Override
  public Website update(Website website) {
    super.update(website);
    Website result = getByUuid(website.getUuid());
    return result;
  }

  @Override
  public boolean updateRootWebpagesOrder(UUID websiteUuid, List<Webpage> rootPages) {
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
