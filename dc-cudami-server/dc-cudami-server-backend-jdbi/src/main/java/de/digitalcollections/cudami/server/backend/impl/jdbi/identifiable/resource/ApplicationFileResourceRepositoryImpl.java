package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.ApplicationFileResourceRepository;
import de.digitalcollections.model.identifiable.resource.ApplicationFileResource;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

@Repository
public class ApplicationFileResourceRepositoryImpl
    extends FileResourceMetadataRepositoryImpl<ApplicationFileResource>
    implements ApplicationFileResourceRepository {

  public static final String MAPPING_PREFIX = "fr";
  public static final String TABLE_ALIAS = "f";
  public static final String TABLE_NAME = "fileresources_application";

  public ApplicationFileResourceRepositoryImpl(
      Jdbi dbi,
      CudamiConfig cudamiConfig,
      IdentifierRepository identifierRepository,
      UrlAliasRepository urlAliasRepository) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        ApplicationFileResource.class,
        cudamiConfig.getOffsetForAlternativePaging(),
        identifierRepository,
        urlAliasRepository);
  }

  @Override
  public ApplicationFileResource create() throws RepositoryException {
    return new ApplicationFileResource();
  }

  @Override
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }
}
