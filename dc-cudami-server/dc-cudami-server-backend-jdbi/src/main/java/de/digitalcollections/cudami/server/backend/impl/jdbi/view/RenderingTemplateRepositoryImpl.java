package de.digitalcollections.cudami.server.backend.impl.jdbi.view;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.view.RenderingTemplateRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.SearchTermTemplates;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.view.RenderingTemplate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

@Repository
public class RenderingTemplateRepositoryImpl extends JdbiRepositoryImpl
    implements RenderingTemplateRepository {

  public static final String MAPPING_PREFIX = "rt";

  public static final String SQL_INSERT_FIELDS =
      " uuid, created, label, description, name, last_modified";
  public static final String SQL_INSERT_VALUES =
      " :uuid, :created, :label::JSONB, :description::JSONB, :name, :lastModified";
  public static final String TABLE_ALIAS = "rt";
  public static final String SQL_REDUCED_FIELDS_RT =
      String.format(
          " %1$s.uuid, %1$s.created, %1$s.label, %1$s.name, %1$s.last_modified, %1$s.description",
          TABLE_ALIAS);
  public static final String SQL_FULL_FIELDS_RT = SQL_REDUCED_FIELDS_RT;
  public static final String TABLE_NAME = "rendering_templates";

  public RenderingTemplateRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX, cudamiConfig.getOffsetForAlternativePaging());
  }

  @Override
  public PageResponse<RenderingTemplate> find(PageRequest pageRequest) {
    StringBuilder commonSql = new StringBuilder(" FROM " + tableName + " AS " + tableAlias);

    Map<String, Object> argumentMappings = new HashMap<>(0);
    String executedSearchTerm = addSearchTerm(pageRequest, commonSql, argumentMappings);

    // Actually "*" should be used in select, but here we don't need it as there is no outer select
    StringBuilder query = new StringBuilder("SELECT " + SQL_REDUCED_FIELDS_RT + commonSql);
    addPageRequestParams(pageRequest, query);
    List<RenderingTemplate> result =
        dbi.withHandle(
            h ->
                h.createQuery(query.toString())
                    .bindMap(argumentMappings)
                    .mapToBean(RenderingTemplate.class)
                    .list());

    long total = count(commonSql.toString(), argumentMappings);

    return new PageResponse(result, pageRequest, total, executedSearchTerm);
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    return new ArrayList<>(Arrays.asList("label", "name"));
  }

  @Override
  public RenderingTemplate getByUuid(UUID uuid) {
    String query =
        "SELECT "
            + SQL_FULL_FIELDS_RT
            + " FROM "
            + tableName
            + " AS "
            + tableAlias
            + " WHERE uuid=:uuid";
    return dbi.withHandle(
        h ->
            h.createQuery(query)
                .bind("uuid", uuid)
                .mapToBean(RenderingTemplate.class)
                .findOne()
                .orElse(null));
  }

  @Override
  public String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "created":
        return tableAlias + ".created";
      case "label":
        return tableAlias + ".label";
      case "lastModified":
        return tableAlias + ".last_modified";
      case "name":
        return tableAlias + ".name";
      case "uuid":
        return tableAlias + ".uuid";
      default:
        return null;
    }
  }

  @Override
  public List<Locale> getLanguages() {
    String query =
        "SELECT DISTINCT jsonb_object_keys("
            + tableAlias
            + ".label) as languages FROM "
            + tableName
            + " AS "
            + tableAlias;
    return dbi.withHandle(h -> h.createQuery(query).mapTo(Locale.class).list());
  }

  @Override
  protected List<String> getSearchTermTemplates(String tableAlias, String originalSearchTerm) {
    return new ArrayList<>(
        Arrays.asList(
            SearchTermTemplates.ILIKE_SEARCH.renderTemplate(tableAlias, "name"),
            SearchTermTemplates.JSONB_PATH.renderTemplate(tableAlias, "label", "**")));
  }

  @Override
  protected String getUniqueField() {
    return "uuid";
  }

  @Override
  public RenderingTemplate save(RenderingTemplate template) {
    template.setUuid(UUID.randomUUID());
    template.setCreated(LocalDateTime.now());
    template.setLastModified(LocalDateTime.now());

    final String query =
        "INSERT INTO "
            + tableName
            + "("
            + SQL_INSERT_FIELDS
            + ")"
            + " VALUES ("
            + SQL_INSERT_VALUES
            + ")"
            + " RETURNING *";

    return dbi.withHandle(
        h ->
            h.createQuery(query)
                .bindBean(template)
                .mapToBean(RenderingTemplate.class)
                .findOne()
                .orElse(null));
  }

  @Override
  protected boolean supportsCaseSensitivityForProperty(String modelProperty) {
    switch (modelProperty) {
      case "label":
        return true;
      default:
        return false;
    }
  }

  @Override
  public RenderingTemplate update(RenderingTemplate template) {
    template.setLastModified(LocalDateTime.now());
    String query =
        "UPDATE "
            + tableName
            + " SET description=:description::JSONB, label=:label::JSONB, last_modified=:lastModified, name=:name"
            + " WHERE uuid=:uuid RETURNING *";
    return dbi.withHandle(
        h ->
            h.createQuery(query)
                .bindBean(template)
                .mapToBean(RenderingTemplate.class)
                .findOne()
                .orElse(null));
  }
}
