package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.agent;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.agent.FamilyNameRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.identifiable.agent.FamilyName;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class FamilyNameRepositoryImpl extends IdentifiableRepositoryImpl<FamilyName>
    implements FamilyNameRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(FamilyNameRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "fn";
  public static final String TABLE_ALIAS = "f";
  public static final String TABLE_NAME = "familynames";

  public FamilyNameRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        FamilyName.class,
        null,
        cudamiConfig.getOffsetForAlternativePaging());
  }

  @Override
  public FamilyName create() throws RepositoryException {
    return new FamilyName();
  }
}
