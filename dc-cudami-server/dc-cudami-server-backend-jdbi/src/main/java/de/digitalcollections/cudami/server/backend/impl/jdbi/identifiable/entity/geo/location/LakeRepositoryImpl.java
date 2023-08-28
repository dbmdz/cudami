package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo.location;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.geo.location.LakeRepository;
import de.digitalcollections.model.identifiable.entity.geo.location.Lake;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class LakeRepositoryImpl extends GeoLocationRepositoryImpl<Lake> implements LakeRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(LakeRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "geo_lk";
  public static final String TABLE_ALIAS = "geo_lk";
  public static final String TABLE_NAME = "geo_lakes";

  public LakeRepositoryImpl(
      Jdbi dbi,
      CudamiConfig cudamiConfig,
      IdentifierRepository identifierRepository,
      UrlAliasRepository urlAliasRepository) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Lake.class,
        cudamiConfig.getOffsetForAlternativePaging(),
        identifierRepository,
        urlAliasRepository);
  }

  @Override
  public Lake create() throws RepositoryException {
    return new Lake();
  }
}
