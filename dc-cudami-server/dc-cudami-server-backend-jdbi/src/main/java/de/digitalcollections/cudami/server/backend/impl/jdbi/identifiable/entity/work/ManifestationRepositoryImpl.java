package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.ManifestationRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.model.identifiable.entity.work.Manifestation;
import java.util.List;
import java.util.Map;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

@Repository
public class ManifestationRepositoryImpl extends EntityRepositoryImpl<Manifestation>
    implements ManifestationRepository {

  /* THIS IS STILL A BIG TODO! */

  public static final String TABLE_NAME = "manifestations"; // TODO
  public static final String TABLE_ALIAS = "mf";
  public static final String MAPPING_PREFIX = "mf";

  @Override
  public String getSqlInsertFields() {
    return super.getSqlInsertFields();
  }

  @Override
  public String getSqlInsertValues() {
    return super.getSqlInsertValues();
  }

  @Override
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  @Override
  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  @Override
  public String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues();
  }

  public ManifestationRepositoryImpl(Jdbi jdbi, CudamiConfig cudamiConfig) {
    super(
        jdbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Manifestation.class,
        cudamiConfig.getOffsetForAlternativePaging());
  }

  @Override
  public String getColumnName(String modelProperty) {
    return super.getColumnName(modelProperty);
    //    switch (modelProperty) {
    //      default:
    //        return super.getColumnName(modelProperty);
    //    }
  }

  @Override
  protected List<String> getSearchTermTemplates(String tableAlias, String originalSearchTerm) {
    // TODO Auto-generated method stub
    return super.getSearchTermTemplates(tableAlias, originalSearchTerm);
  }

  @Override
  protected String addSearchTermMappings(String searchTerm, Map<String, Object> argumentMappings) {
    // TODO Auto-generated method stub
    return super.addSearchTermMappings(searchTerm, argumentMappings);
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    // TODO Auto-generated method stub
    return super.getAllowedOrderByFields();
  }

  @Override
  protected boolean supportsCaseSensitivityForProperty(String modelProperty) {
    // TODO Auto-generated method stub
    return super.supportsCaseSensitivityForProperty(modelProperty);
  }
}
