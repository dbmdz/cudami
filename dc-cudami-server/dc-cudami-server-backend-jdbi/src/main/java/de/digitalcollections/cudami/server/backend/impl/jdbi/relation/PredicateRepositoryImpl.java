package de.digitalcollections.cudami.server.backend.impl.jdbi.relation;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.relation.PredicateRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.UniqueObjectRepositoryImpl;
import de.digitalcollections.model.relation.Predicate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.springframework.stereotype.Repository;

@Repository
public class PredicateRepositoryImpl extends UniqueObjectRepositoryImpl<Predicate>
    implements PredicateRepository {

  public static final String MAPPING_PREFIX = "pred";
  public static final String TABLE_ALIAS = "pred";
  public static final String TABLE_NAME = "predicates";

  public PredicateRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Predicate.class,
        cudamiConfig.getOffsetForAlternativePaging());
    dbi.registerRowMapper(BeanMapper.factory(Predicate.class, MAPPING_PREFIX));
  }

  @Override
  public Predicate create() throws RepositoryException {
    return new Predicate();
  }

  @Override
  public boolean deleteByValue(String value) throws RepositoryException {
    int count =
        dbi.withHandle(
            h ->
                h.createUpdate("DELETE FROM " + tableName + " WHERE value = :value")
                    .bind("value", value)
                    .execute());
    return count == 1;
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    List<String> allowedOrderByFields = super.getAllowedOrderByFields();
    allowedOrderByFields.addAll(Arrays.asList("label", "value"));
    return allowedOrderByFields;
  }

  @Override
  public Predicate getByValue(String value) throws RepositoryException {
    String query =
        "SELECT "
            + getSqlSelectAllFields()
            + " FROM "
            + tableName
            + " AS "
            + tableAlias
            + " WHERE value = :value";
    Optional<Predicate> result =
        dbi.withHandle(
            h -> h.createQuery(query).bind("value", value).mapTo(Predicate.class).findFirst());
    return result.orElse(null);
  }

  @Override
  public String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "description":
        return tableAlias + ".description";
      case "label":
        return tableAlias + ".label";
      case "value":
        return tableAlias + ".value";
      default:
        return super.getColumnName(modelProperty);
    }
  }

  @Override
  protected LinkedHashMap<String, Function<Predicate, Optional<Object>>> getJsonbFields() {
    LinkedHashMap<String, Function<Predicate, Optional<Object>>> jsonbFields =
        super.getJsonbFields();
    jsonbFields.put("description", i -> Optional.ofNullable(i.getDescription()));
    jsonbFields.put("label", i -> Optional.ofNullable(i.getLabel()));
    return jsonbFields;
  }

  @Override
  public List<Locale> getLanguages() throws RepositoryException {
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
  public List<Predicate> getRandom(int count) throws RepositoryException {
    throw new UnsupportedOperationException(); // TODO: not yet implemented
  }

  @Override
  protected String getSqlInsertFields() {
    return super.getSqlInsertFields() + ", description, label, value";
  }

  @Override
  protected String getSqlInsertValues() {
    return super.getSqlInsertValues() + ", :description::JSONB, :label::JSONB, :value";
  }

  @Override
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".description "
        + mappingPrefix
        + "_description";
  }

  @Override
  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".label "
        + mappingPrefix
        + "_label, "
        + tableAlias
        + ".value "
        + mappingPrefix
        + "_value";
  }

  @Override
  protected String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues()
        + ", description=:description::JSONB, label=:label::JSONB, value=:value";
  }

  @Override
  protected String getUniqueField() {
    return "value";
  }

  @Override
  protected boolean supportsCaseSensitivityForProperty(String modelProperty) {
    switch (modelProperty) {
      case "label":
      case "description":
        return true;
      default:
        return false;
    }
  }
}
