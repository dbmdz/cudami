package de.digitalcollections.cudami.server.backend.impl.lobid.identifiable.entity;

import de.digitalcollections.cudami.lobid.client.LobidClient;
import de.digitalcollections.cudami.lobid.client.LobidCorporationsClient;
import de.digitalcollections.cudami.lobid.client.exceptions.HttpException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.ExternalCorporationRepository;
import de.digitalcollections.model.api.identifiable.entity.Corporation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class ExternalCorporationRepositoryImpl implements ExternalCorporationRepository {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ExternalCorporationRepositoryImpl.class);

  LobidCorporationsClient client;

  public ExternalCorporationRepositoryImpl(LobidClient lobidClient) {
    this.client = lobidClient.forCorporations();
  }

  @Override
  public Corporation getByGndId(String gndId) {
    try {
      Corporation corporation = client.getByGndId(gndId);
      return corporation;
    } catch (HttpException ex) {
      LOGGER.warn("Could not get Corporation by GND-ID: " + gndId, ex);
    }
    return null;
  }
}
