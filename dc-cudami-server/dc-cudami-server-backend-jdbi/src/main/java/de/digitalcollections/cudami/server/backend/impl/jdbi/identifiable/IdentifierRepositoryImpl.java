package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.UniqueObjectRepositoryImpl;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.validation.ValidationException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.springframework.stereotype.Repository;

@Repository
public class IdentifierRepositoryImpl extends UniqueObjectRepositoryImpl<Identifier>
    implements IdentifierRepository {

  public static final String MAPPING_PREFIX = "id";
  public static final String TABLE_ALIAS = "id";
  public static final String TABLE_NAME = "identifiers";

  public static String sqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return sqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  public static String sqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return UniqueObjectRepositoryImpl.sqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".identifiable "
        + mappingPrefix
        + "_identifiable, "
        + tableAlias
        + ".namespace "
        + mappingPrefix
        + "_namespace, "
        + tableAlias
        + ".identifier "
        + mappingPrefix
        + "_id";
  }

  public IdentifierRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Identifier.class,
        cudamiConfig.getOffsetForAlternativePaging());

    // registering row mapper for Identifier
    dbi.registerRowMapper(BeanMapper.factory(Identifier.class, MAPPING_PREFIX));
  }

  @Override
  public Identifier create() throws RepositoryException {
    return new Identifier();
  }

  @Override
  public int deleteByIdentifiable(UUID identifiableUuid) throws RepositoryException {
    final String sql = "DELETE FROM " + tableName + " WHERE identifiable = :uuid";
    HashMap<String, Object> bindings = new HashMap<>(0);
    bindings.put("uuid", identifiableUuid);
    return execUpdateWithMap(sql, bindings);
  }

  @Override
  public List<Identifier> findByIdentifiable(UUID uuidIdentifiable) throws RepositoryException {
    final String sql =
        "SELECT "
            + getSqlSelectReducedFields()
            + " FROM "
            + tableName
            + " "
            + tableAlias
            + " WHERE identifiable = :uuid";
    return execSelectForList(sql, Map.of("uuid", uuidIdentifiable));
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    List<String> allowedOrderByFields = super.getAllowedOrderByFields();
    allowedOrderByFields.addAll(Arrays.asList("id", "identifiable", "namespace"));
    return allowedOrderByFields;
  }

  @Override
  public String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "id":
        return tableAlias + ".identifier";
      case "identifiable":
        return tableAlias + ".identifiable";
      case "namespace":
        return tableAlias + ".namespace";
      default:
        return super.getColumnName(modelProperty);
    }
  }

  @Override
  public List<Identifier> getRandom(int count) throws RepositoryException {
    throw new UnsupportedOperationException(); // TODO: not yet implemented
  }

  @Override
  protected String getSqlInsertFields() {
    return super.getSqlInsertFields() + ", identifiable, namespace, identifier";
  }

  @Override
  protected String getSqlInsertValues() {
    return super.getSqlInsertValues() + ", :identifiable, :namespace, :id";
  }

  @Override
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return sqlSelectAllFields(tableAlias, mappingPrefix);
  }

  @Override
  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return sqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  @Override
  protected String getSqlUpdateFieldValues() {
    // do not update/left out from statement (not changed since insert):
    // uuid, created
    return super.getSqlUpdateFieldValues()
        + ", identifiable=:identifiable, namespace=:namespace, identifier=:id";
  }

  @Override
  public Set<Identifier> saveForIdentifiable(Identifiable identifiable, Set<Identifier> identifiers)
      throws RepositoryException, ValidationException {
    if (identifiers == null) {
      return new HashSet<>(0);
    }
    Set<Identifier> savedIdentifiers = new HashSet<>(identifiers.size());
    for (Identifier identifier : identifiers) {
      if (identifier.getUuid() == null) {
        Map<String, Object> bindings = new HashMap<>();
        bindings.put("identifiable", identifiable.getUuid());
        save(identifier, bindings);
        savedIdentifiers.add(identifier);
      } else {
        Identifier dbIdentifier = getByUuid(identifier.getUuid());
        savedIdentifiers.add(dbIdentifier);
      }
    }
    return savedIdentifiers;
  }

  @Override
  protected boolean supportsCaseSensitivityForProperty(String modelProperty) {
    return false;
  }
}
