package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.TextFileResourceRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.api.identifiable.resource.TextFileResource;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.impl.identifiable.parts.LocalizedTextImpl;
import de.digitalcollections.model.impl.identifiable.resource.TextFileResourceImpl;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TextFileResourceRepositoryImpl extends IdentifiableRepositoryImpl<TextFileResource>
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

  private final FileResourceMetadataRepositoryImpl metadataRepository;

  @Autowired
  public TextFileResourceRepositoryImpl(
      Jdbi dbi,
      IdentifierRepository identifierRepository,
      FileResourceMetadataRepositoryImpl fileResourceMetadataRepositoryImpl) {
    super(
        dbi,
        identifierRepository,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        TextFileResourceImpl.class,
        getSqlSelectAllFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlSelectReducedFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlInsertFields(),
        getSqlInsertValues(),
        getSqlUpdateFieldValues());
    this.metadataRepository = fileResourceMetadataRepositoryImpl;
  }

  @Override
  public SearchPageResponse<TextFileResource> find(SearchPageRequest searchPageRequest) {
    String commonSql = metadataRepository.getCommonFileResourceSearchSql(tableName, tableAlias);
    return find(searchPageRequest, commonSql, Map.of("searchTerm", searchPageRequest.getQuery()));
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    return metadataRepository.getAllowedOrderByFields();
  }

  @Override
  public TextFileResource save(TextFileResource fileResource) {
    if (fileResource.getLabel() == null && fileResource.getFilename() != null) {
      // set a default label = filename (an empty label violates constraint)
      fileResource.setLabel(new LocalizedTextImpl(Locale.ROOT, fileResource.getFilename()));
    }
    super.save(fileResource);
    TextFileResource result = findOne(fileResource.getUuid());
    return result;
  }

  @Override
  public TextFileResource update(TextFileResource fileResource) {
    super.update(fileResource);
    TextFileResource result = findOne(fileResource.getUuid());
    return result;
  }
}
