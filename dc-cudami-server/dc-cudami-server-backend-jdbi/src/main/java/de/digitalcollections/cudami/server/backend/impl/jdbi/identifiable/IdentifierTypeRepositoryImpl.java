package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierTypeRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.UniqueObjectRepositoryImpl;
import de.digitalcollections.model.identifiable.IdentifierType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

@Repository
public class IdentifierTypeRepositoryImpl extends UniqueObjectRepositoryImpl<IdentifierType>
    implements IdentifierTypeRepository {

  public static final String MAPPING_PREFIX = "idt";
  public static final String TABLE_ALIAS = "idt";
  public static final String TABLE_NAME = "identifiertypes";

  public IdentifierTypeRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        IdentifierType.class,
        cudamiConfig.getOffsetForAlternativePaging());
  }

  @Override
  public IdentifierType create() throws RepositoryException {
    return new IdentifierType();
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    List<String> allowedOrderByFields = super.getAllowedOrderByFields();
    allowedOrderByFields.addAll(Arrays.asList("label", "namespace", "pattern"));
    return allowedOrderByFields;
  }

  @Override
  public IdentifierType getByNamespace(String namespace) {
    final String sql = "SELECT * FROM " + tableName + " WHERE namespace = :namespace";

    IdentifierType identifierType =
        dbi.withHandle(
            h ->
                h.createQuery(sql)
                    .bind("namespace", namespace)
                    .mapToBean(IdentifierType.class)
                    .findOne()
                    .orElse(null));

    return identifierType;
  }

  @Override
  public String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "label":
        return tableAlias + ".label";
      case "namespace":
        return tableAlias + ".namespace";
      case "pattern":
        return tableAlias + ".pattern";
      default:
        return super.getColumnName(modelProperty);
    }
  }

  @Override
  public List<IdentifierType> getRandom(int count) throws RepositoryException {
    throw new UnsupportedOperationException(); // TODO: not yet implemented
  }

  @Override
  // FIXME: still needed?
  protected List<String> getSearchTermTemplates(String tableAlias, String originalSearchTerm) {
    return new ArrayList<>(
        Arrays.asList(
            SearchTermTemplates.ILIKE_SEARCH.renderTemplate(tableAlias, "label"),
            SearchTermTemplates.ILIKE_SEARCH.renderTemplate(tableAlias, "namespace")));
  }

  @Override
  protected String getSqlInsertFields() {
    return super.getSqlInsertFields() + ", label, namespace, pattern";
  }

  @Override
  protected String getSqlInsertValues() {
    return super.getSqlInsertValues() + ", :label, :namespace, :pattern";
  }

  @Override
  protected String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".label "
        + mappingPrefix
        + "_label, "
        + tableAlias
        + ".namespace "
        + mappingPrefix
        + "_namespace, "
        + tableAlias
        + ".pattern "
        + mappingPrefix
        + "_pattern";
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
}
