package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo.location;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.geo.location.CreekRepository;
import de.digitalcollections.model.identifiable.entity.geo.location.Creek;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class CreekRepositoryImpl extends GeoLocationRepositoryImpl<Creek>
    implements CreekRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreekRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "geo_crk";
  public static final String TABLE_ALIAS = "geo_crk";
  public static final String TABLE_NAME = "geo_creeks";

  public CreekRepositoryImpl(
      Jdbi dbi,
      CudamiConfig cudamiConfig,
      IdentifierRepository identifierRepository,
      UrlAliasRepository urlAliasRepository) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Creek.class,
        cudamiConfig.getOffsetForAlternativePaging(),
        identifierRepository,
        urlAliasRepository);
  }

  @Override
  public Creek create() throws RepositoryException {
    return new Creek();
  }
}
