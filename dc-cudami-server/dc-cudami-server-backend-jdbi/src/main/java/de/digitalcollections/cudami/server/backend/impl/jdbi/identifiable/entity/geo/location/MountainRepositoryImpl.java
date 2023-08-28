package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo.location;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.geo.location.MountainRepository;
import de.digitalcollections.model.identifiable.entity.geo.location.Mountain;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class MountainRepositoryImpl extends GeoLocationRepositoryImpl<Mountain>
    implements MountainRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(MountainRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "geo_mt";
  public static final String TABLE_ALIAS = "geo_mt";
  public static final String TABLE_NAME = "geo_mountains";

  public MountainRepositoryImpl(
      Jdbi dbi,
      CudamiConfig cudamiConfig,
      IdentifierRepository identifierRepository,
      UrlAliasRepository urlAliasRepository) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Mountain.class,
        cudamiConfig.getOffsetForAlternativePaging(),
        identifierRepository,
        urlAliasRepository);
  }

  @Override
  public Mountain create() throws RepositoryException {
    return new Mountain();
  }

  @Override
  protected String getSqlInsertFields() {
    return super.getSqlInsertFields() + ", height";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  @Override
  protected String getSqlInsertValues() {
    return super.getSqlInsertValues() + ", :height";
  }

  @Override
  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".height "
        + mappingPrefix
        + "_height";
  }

  @Override
  protected String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues() + ", height=:height";
  }
}
