package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.AudioFileResourceRepository;
import de.digitalcollections.model.identifiable.resource.AudioFileResource;
import java.util.List;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AudioFileResourceRepositoryImpl
    extends FileResourceMetadataRepositoryImpl<AudioFileResource>
    implements AudioFileResourceRepository {

  public static final String MAPPING_PREFIX = "fr";
  public static final String TABLE_ALIAS = "f";
  public static final String TABLE_NAME = "fileresources_audio";

  @Override
  public String getSqlInsertFields() {
    return super.getSqlInsertFields() + ", duration";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  @Override
  public String getSqlInsertValues() {
    return super.getSqlInsertValues() + ", :duration";
  }

  @Override
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".duration "
        + mappingPrefix
        + "_duration";
  }

  @Override
  public String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues() + ", duration=:duration";
  }

  @Autowired
  public AudioFileResourceRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        AudioFileResource.class,
        cudamiConfig.getOffsetForAlternativePaging());
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    List<String> allowedOrderByFields = super.getAllowedOrderByFields();
    allowedOrderByFields.add("duration");
    return allowedOrderByFields;
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
      case "duration":
        return tableAlias + ".duration";
      default:
        return null;
    }
  }
}
