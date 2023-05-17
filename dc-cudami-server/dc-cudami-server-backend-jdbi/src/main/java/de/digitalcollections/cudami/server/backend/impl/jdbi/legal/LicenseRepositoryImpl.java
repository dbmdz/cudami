package de.digitalcollections.cudami.server.backend.impl.jdbi.legal;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.legal.LicenseRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.UniqueObjectRepositoryImpl;
import de.digitalcollections.model.legal.License;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class LicenseRepositoryImpl extends UniqueObjectRepositoryImpl<License>
    implements LicenseRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(LicenseRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "l";
  public static final String TABLE_ALIAS = "li";
  public static final String TABLE_NAME = "licenses";

  public LicenseRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        License.class,
        cudamiConfig.getOffsetForAlternativePaging());
    dbi.registerRowMapper(BeanMapper.factory(License.class, MAPPING_PREFIX));
  }

  @Override
  public License create() throws RepositoryException {
    return new License();
  }

  @Override
  public void deleteByUrl(URL url) {
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM " + tableName + " WHERE url = :url")
                .bind("url", url)
                .execute());
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    List<String> allowedOrderByFields = super.getAllowedOrderByFields();
    allowedOrderByFields.addAll(Arrays.asList("acronym", "label", "url"));
    return allowedOrderByFields;
  }

  @Override
  public License getByUrl(URL url) {
    String query = "SELECT * FROM " + tableName + " WHERE url=:url";
    return dbi.withHandle(
        h -> h.createQuery(query).bind("url", url).mapToBean(License.class).findOne().orElse(null));
  }

  @Override
  public String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "acronym":
        return tableAlias + ".acronym";
      case "label":
        return tableAlias + ".label";
      case "url":
        return tableAlias + ".url";
      default:
        return super.getColumnName(modelProperty);
    }
  }

  @Override
  protected LinkedHashMap<String, Function<License, Optional<Object>>> getJsonbFields() {
    LinkedHashMap<String, Function<License, Optional<Object>>> jsonbFields = super.getJsonbFields();
    jsonbFields.put("label", i -> Optional.ofNullable(i.getLabel()));
    return jsonbFields;
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
  public List<License> getRandom(int count) throws RepositoryException {
    throw new UnsupportedOperationException(); // TODO: not yet implemented
  }

  @Override
  protected String getSqlInsertFields() {
    return super.getSqlInsertFields() + ", acronym, label, url";
  }

  @Override
  protected String getSqlInsertValues() {
    return super.getSqlInsertValues() + ", :acronym, :label::JSONB, :url";
  }

  @Override
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  @Override
  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".acronym "
        + mappingPrefix
        + "_acronym, "
        + tableAlias
        + ".label "
        + mappingPrefix
        + "_label, "
        + tableAlias
        + ".url "
        + mappingPrefix
        + "_url";
  }

  @Override
  protected String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues() + ", acronym=:acronym, label=:label::JSONB, url=:url";
  }

  @Override
  protected boolean supportsCaseSensitivityForProperty(String modelProperty) {
    switch (modelProperty) {
      case "acronym":
      case "label":
        return true;
      default:
        return false;
    }
  }
}
