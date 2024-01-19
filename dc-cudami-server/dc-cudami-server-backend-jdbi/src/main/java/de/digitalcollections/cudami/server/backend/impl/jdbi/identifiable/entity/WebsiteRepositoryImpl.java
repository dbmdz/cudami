package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.WebsiteRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.web.WebpageRepositoryImpl;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class WebsiteRepositoryImpl extends EntityRepositoryImpl<Website>
    implements WebsiteRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebsiteRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "webs";
  public static final String TABLE_ALIAS = "webs";
  public static final String TABLE_NAME = "websites";

  private final WebpageRepositoryImpl webpageRepositoryImpl;

  public WebsiteRepositoryImpl(
      Jdbi dbi,
      CudamiConfig cudamiConfig,
      IdentifierRepository identifierRepository,
      UrlAliasRepository urlAliasRepository,
      WebpageRepositoryImpl webpageRepositoryImpl) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Website.class,
        cudamiConfig.getOffsetForAlternativePaging(),
        identifierRepository,
        urlAliasRepository);
    this.webpageRepositoryImpl = webpageRepositoryImpl;
  }

  @Override
  public Website create() throws RepositoryException {
    return new Website();
  }

  @Override
  public PageResponse<Webpage> findRootWebpages(UUID uuid, PageRequest pageRequest) {
    throw new UnsupportedOperationException(); // TODO: paging not yet implemented
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    List<String> allowedOrderByFields = super.getAllowedOrderByFields();
    allowedOrderByFields.addAll(Arrays.asList("url"));
    return allowedOrderByFields;
  }

  @Override
  public Website getByIdentifier(Identifier identifier) throws RepositoryException {
    Website website = super.getByIdentifier(identifier);

    if (website != null) {
      website.setRootPages(getRootWebpages(website.getUuid()));
    }
    return website;
  }

  @Override
  public List<Website> getByUuidsAndFiltering(List<UUID> uuids, Filtering filtering)
      throws RepositoryException {
    List<Website> websites = super.getByUuidsAndFiltering(uuids, filtering);

    if (websites != null && !websites.isEmpty()) {
      for (Website website : websites) {
        website.setRootPages(getRootWebpages(website.getUuid()));
      }
    }

    return websites;
  }

  @Override
  public String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "url":
        return tableAlias + ".url";
      case "registrationDate":
        return tableAlias + ".registration_date";
      default:
        return super.getColumnName(modelProperty);
    }
  }

  @Override
  public List<Webpage> getRootWebpages(UUID uuid) throws RepositoryException {
    final String wpTableAlias = webpageRepositoryImpl.getTableAlias();
    final String wpTableName = webpageRepositoryImpl.getTableName();

    StringBuilder innerQuery =
        new StringBuilder(
            """
            SELECT {{webpageAlias}}.*, ww.sortindex wpidx FROM {{webpage}} {{webpageAlias}}
                INNER JOIN website_webpages ww ON {{webpageAlias}}.uuid = ww.webpage_uuid
            WHERE ww.website_uuid = :uuid
            """
                .replace("{{webpage}}", wpTableName)
                .replace("{{webpageAlias}}", wpTableAlias));
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", uuid);

    return webpageRepositoryImpl.retrieveList(
        webpageRepositoryImpl.getSqlSelectReducedFields(),
        innerQuery,
        argumentMappings,
        "%s.wpidx".formatted(wpTableAlias));
  }

  @Override
  protected String getSqlInsertFields() {
    return super.getSqlInsertFields() + ", registration_date, url";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  @Override
  protected String getSqlInsertValues() {
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
