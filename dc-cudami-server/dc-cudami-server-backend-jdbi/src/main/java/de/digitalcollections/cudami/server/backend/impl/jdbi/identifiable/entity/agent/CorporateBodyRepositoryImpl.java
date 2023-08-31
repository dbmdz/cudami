package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.CorporateBodyRepository;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class CorporateBodyRepositoryImpl extends AgentRepositoryImpl<CorporateBody>
    implements CorporateBodyRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(CorporateBodyRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "cb";
  public static final String TABLE_ALIAS = "c";
  public static final String TABLE_NAME = "corporatebodies";

  public CorporateBodyRepositoryImpl(
      Jdbi dbi,
      CudamiConfig cudamiConfig,
      IdentifierRepository identifierRepository,
      UrlAliasRepository urlAliasRepository) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        CorporateBody.class,
        cudamiConfig.getOffsetForAlternativePaging(),
        identifierRepository,
        urlAliasRepository);
  }

  @Override
  public CorporateBody create() throws RepositoryException {
    return new CorporateBody();
  }

  @Override
  public String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "homepageUrl":
        return tableAlias + ".homepage_url";
      default:
        return super.getColumnName(modelProperty);
    }
  }

  @Override
  public String getSqlInsertFields() {
    return super.getSqlInsertFields() + ", homepage_url, text";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  @Override
  public String getSqlInsertValues() {
    return super.getSqlInsertValues() + ", :homepageUrl, :text::JSONB";
  }

  @Override
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectAllFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".text "
        + mappingPrefix
        + "_text";
  }

  @Override
  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".homepage_url "
        + mappingPrefix
        + "_homepageUrl";
  }

  @Override
  public String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues() + ", homepage_url=:homepageUrl, text=:text::JSONB";
  }

  @Override
  public List<CorporateBody> findCollectionRelatedCorporateBodies(
      UUID collectionUuid, Filtering filtering) throws RepositoryException {
    // We do a double join with "rel_entity_entities" because we have two different
    // predicates:
    // - one is fix ("is_part_of"): defines the relation between collection and
    // project
    // - the other one is given as part of the parameter "filtering" for defining
    // relation
    // between corporatebody and project
    StringBuilder innerQuery =
        new StringBuilder(
            """
              SELECT * FROM {{tableName}} {{tableAlias}}
              LEFT JOIN rel_entity_entities AS r ON {{tableAlias}}.uuid = r.object_uuid
              LEFT JOIN rel_entity_entities AS rel ON r.subject_uuid = rel.subject_uuid
              WHERE rel.object_uuid = :uuid
                AND rel.predicate = 'is_part_of' """
                .replace("{{tableName}}", tableName)
                .replace("{{tableAlias}}", tableAlias));
    FilterCriterion predicate =
        filtering != null ? filtering.getFilterCriterionFor("predicate") : null;
    if (predicate != null) {
      String predicateFilter = String.format(" AND r.predicate = '%s'", predicate.getValue());
      innerQuery.append(predicateFilter);
    }

    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", collectionUuid);
    List<CorporateBody> result =
        retrieveList(getSqlSelectReducedFields(), innerQuery, argumentMappings, null);

    return result;
  }

  @Override
  public PageResponse<CorporateBody> findCollectionRelatedCorporateBodies(
      UUID collectionUuid, PageRequest pageRequest) {
    throw new UnsupportedOperationException(); // TODO: not yet implemented
  }
}
