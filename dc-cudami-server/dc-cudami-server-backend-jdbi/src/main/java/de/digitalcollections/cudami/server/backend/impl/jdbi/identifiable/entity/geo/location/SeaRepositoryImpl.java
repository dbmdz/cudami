package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo.location;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.geo.location.SeaRepository;
import de.digitalcollections.model.identifiable.entity.geo.location.Sea;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class SeaRepositoryImpl extends GeoLocationRepositoryImpl<Sea> implements SeaRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(SeaRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "geo_sea";
  public static final String TABLE_ALIAS = "geo_sea";
  public static final String TABLE_NAME = "geo_seas";

  public SeaRepositoryImpl(
      Jdbi dbi,
      CudamiConfig cudamiConfig,
      IdentifierRepository identifierRepository,
      UrlAliasRepository urlAliasRepository) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Sea.class,
        cudamiConfig.getOffsetForAlternativePaging(),
        identifierRepository,
        urlAliasRepository);
  }

  @Override
  public Sea create() throws RepositoryException {
    return new Sea();
  }
}
