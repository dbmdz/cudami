package de.digitalcollections.cudami.server.backend.impl.lobid.identifiable.entity.agent;

import de.digitalcollections.cudami.lobid.client.LobidClient;
import de.digitalcollections.cudami.lobid.client.LobidCorporateBodiesClient;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.ExternalCorporateBodyRepository;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class ExternalCorporateBodyRepositoryImpl implements ExternalCorporateBodyRepository {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ExternalCorporateBodyRepositoryImpl.class);

  LobidCorporateBodiesClient client;

  public ExternalCorporateBodyRepositoryImpl(LobidClient lobidClient) {
    this.client = lobidClient.forCorporateBodies();
  }

  @Override
  public CorporateBody getByGndId(String gndId) {
    try {
      CorporateBody corporateBody = client.getByGndId(gndId);
      return corporateBody;
    } catch (TechnicalException ex) {
      LOGGER.warn("Could not get corporate body by GND-ID: " + gndId, ex);
    }
    return null;
  }
}
