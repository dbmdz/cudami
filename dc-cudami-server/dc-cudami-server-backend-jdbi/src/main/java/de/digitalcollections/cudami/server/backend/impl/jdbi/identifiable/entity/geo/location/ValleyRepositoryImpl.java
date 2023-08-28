package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo.location;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.geo.location.ValleyRepository;
import de.digitalcollections.model.identifiable.entity.geo.location.Valley;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class ValleyRepositoryImpl extends GeoLocationRepositoryImpl<Valley>
    implements ValleyRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(ValleyRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "geo_vy";
  public static final String TABLE_ALIAS = "geo_vy";
  public static final String TABLE_NAME = "geo_valleys";

  public ValleyRepositoryImpl(
      Jdbi dbi,
      CudamiConfig cudamiConfig,
      IdentifierRepository identifierRepository,
      UrlAliasRepository urlAliasRepository) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Valley.class,
        cudamiConfig.getOffsetForAlternativePaging(),
        identifierRepository,
        urlAliasRepository);
  }

  @Override
  public Valley create() throws RepositoryException {
    return new Valley();
  }
}
