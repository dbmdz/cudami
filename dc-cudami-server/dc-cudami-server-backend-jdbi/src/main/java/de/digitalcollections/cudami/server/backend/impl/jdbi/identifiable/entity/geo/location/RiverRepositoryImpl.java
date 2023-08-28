package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo.location;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.geo.location.RiverRepository;
import de.digitalcollections.model.identifiable.entity.geo.location.River;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class RiverRepositoryImpl extends GeoLocationRepositoryImpl<River>
    implements RiverRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(RiverRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "geo_rv";
  public static final String TABLE_ALIAS = "geo_rv";
  public static final String TABLE_NAME = "geo_rivers";

  public RiverRepositoryImpl(
      Jdbi dbi,
      CudamiConfig cudamiConfig,
      IdentifierRepository identifierRepository,
      UrlAliasRepository urlAliasRepository) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        River.class,
        cudamiConfig.getOffsetForAlternativePaging(),
        identifierRepository,
        urlAliasRepository);
  }

  @Override
  public River create() throws RepositoryException {
    return new River();
  }
}
