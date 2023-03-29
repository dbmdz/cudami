package de.digitalcollections.cudami.server.backend.impl.jdbi.semantic;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.semantic.TagRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.UniqueObjectRepositoryImpl;
import de.digitalcollections.model.semantic.Tag;
import java.util.Arrays;
import java.util.List;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TagRepositoryImpl extends UniqueObjectRepositoryImpl<Tag> implements TagRepository {

  public static final String MAPPING_PREFIX = "tags";
  public static final String TABLE_ALIAS = "tags";
  public static final String TABLE_NAME = "tags";

  private static TagRepositoryImpl SINGLETON_INSTANCE = new TagRepositoryImpl();

  /**
   * constructor for static methods to make access possible to instance fields that do not use
   * further dependencies, see {@link #getSqlSelectAllFieldsStatic()}
   */
  private TagRepositoryImpl() {
    super();
  }

  @Autowired
  public TagRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Tag.class,
        cudamiConfig.getOffsetForAlternativePaging());
    dbi.registerRowMapper(BeanMapper.factory(Tag.class, MAPPING_PREFIX));
  }

  @Override
  public Tag create() throws RepositoryException {
    return new Tag();
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    List<String> allowedOrderByFields = super.getAllowedOrderByFields();
    allowedOrderByFields.addAll(Arrays.asList("value"));
    return allowedOrderByFields;
  }

  @Override
  public Tag getByValue(String value) {
    final String sql =
        "SELECT " + getSqlSelectAllFields() + " FROM " + tableName + " WHERE value = :value";

    Tag tag =
        dbi.withHandle(
            h -> h.createQuery(sql).bind("value", value).mapTo(Tag.class).findOne().orElse(null));

    return tag;
  }

  @Override
  public String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "value":
        return tableAlias + ".value";
      default:
        return super.getColumnName(modelProperty);
    }
  }

  @Override
  public List<Tag> getRandom(int count) throws RepositoryException {
    throw new UnsupportedOperationException(); // TODO: not yet implemented
  }

  @Override
  protected String getSqlInsertFields() {
    return super.getSqlInsertFields() + ", value";
  }

  @Override
  protected String getSqlInsertValues() {
    return super.getSqlInsertValues() + ", :value";
  }

  @Override
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  public static String getSqlSelectAllFieldsStatic() {
    return SINGLETON_INSTANCE.getSqlSelectAllFields(TABLE_ALIAS, MAPPING_PREFIX);
  }

  @Override
  protected String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".value "
        + mappingPrefix
        + "_value";
  }

  public static String getSqlSelectReducedFieldsStatic() {
    return SINGLETON_INSTANCE.getSqlSelectReducedFields(TABLE_ALIAS, MAPPING_PREFIX);
  }

  @Override
  public String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues() + ", value=:value";
  }

  @Override
  protected boolean supportsCaseSensitivityForProperty(String modelProperty) {
    switch (modelProperty) {
      case "value":
        return true;
      default:
        return false;
    }
  }
}
