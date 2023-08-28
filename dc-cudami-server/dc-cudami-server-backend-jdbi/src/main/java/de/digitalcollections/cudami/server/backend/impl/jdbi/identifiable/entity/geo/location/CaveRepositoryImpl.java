package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo.location;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.geo.location.CaveRepository;
import de.digitalcollections.model.identifiable.entity.geo.location.Cave;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class CaveRepositoryImpl extends GeoLocationRepositoryImpl<Cave> implements CaveRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(CaveRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "geo_cv";
  public static final String TABLE_ALIAS = "geo_cv";
  public static final String TABLE_NAME = "geo_caves";

  public CaveRepositoryImpl(
      Jdbi dbi,
      CudamiConfig cudamiConfig,
      IdentifierRepository identifierRepository,
      UrlAliasRepository urlAliasRepository) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Cave.class,
        cudamiConfig.getOffsetForAlternativePaging(),
        identifierRepository,
        urlAliasRepository);
  }

  @Override
  public Cave create() throws RepositoryException {
    return new Cave();
  }
}
