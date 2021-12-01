package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.AudioFileResourceRepository;
import de.digitalcollections.model.identifiable.resource.AudioFileResource;
import java.util.List;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AudioFileResourceRepositoryImpl
    extends FileResourceMetadataRepositoryImpl<AudioFileResource>
    implements AudioFileResourceRepository {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(AudioFileResourceRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "fr";
  public static final String TABLE_ALIAS = "f";
  public static final String TABLE_NAME = "fileresources_audio";

  public static String getSqlInsertFields() {
    return FileResourceMetadataRepositoryImpl.getSqlInsertFields() + ", duration";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  public static String getSqlInsertValues() {
    return FileResourceMetadataRepositoryImpl.getSqlInsertValues() + ", :duration";
  }

  public static String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".duration "
        + mappingPrefix
        + "_duration";
  }

  public static String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return FileResourceMetadataRepositoryImpl.getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  public static String getSqlUpdateFieldValues() {
    return FileResourceMetadataRepositoryImpl.getSqlUpdateFieldValues() + ", duration=:duration";
  }

  @Autowired
  public AudioFileResourceRepositoryImpl(Jdbi dbi) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        AudioFileResource.class,
        getSqlSelectAllFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlSelectReducedFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlInsertFields(),
        getSqlInsertValues(),
        getSqlUpdateFieldValues());
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    List<String> allowedOrderByFields = super.getAllowedOrderByFields();
    allowedOrderByFields.add("duration");
    return allowedOrderByFields;
  }

  @Override
  protected String getColumnName(String modelProperty) {
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
