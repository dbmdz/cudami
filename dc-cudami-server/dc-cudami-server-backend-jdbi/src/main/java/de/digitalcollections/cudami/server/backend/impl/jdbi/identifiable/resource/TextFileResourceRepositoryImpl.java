package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.TextFileResourceRepository;
import de.digitalcollections.model.identifiable.resource.TextFileResource;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TextFileResourceRepositoryImpl
    extends FileResourceMetadataRepositoryImpl<TextFileResource>
    implements TextFileResourceRepository {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(TextFileResourceRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "fr";
  public static final String TABLE_ALIAS = "f";
  public static final String TABLE_NAME = "fileresources_text";

  public static String getSqlInsertFields() {
    return FileResourceMetadataRepositoryImpl.getSqlInsertFields();
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  public static String getSqlInsertValues() {
    return FileResourceMetadataRepositoryImpl.getSqlInsertValues();
  }

  public static String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  public static String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return FileResourceMetadataRepositoryImpl.getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  public static String getSqlUpdateFieldValues() {
    return FileResourceMetadataRepositoryImpl.getSqlUpdateFieldValues();
  }

  @Autowired
  public TextFileResourceRepositoryImpl(Jdbi dbi) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        TextFileResource.class,
        getSqlSelectAllFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlSelectReducedFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlInsertFields(),
        getSqlInsertValues(),
        getSqlUpdateFieldValues());
  }
}
