package de.digitalcollections.cudami.server.backend.impl.lobid.identifiable.entity;

import de.digitalcollections.cudami.lobid.client.LobidCorporationsClient;
import de.digitalcollections.cudami.lobid.client.exceptions.HttpException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.ExternalCorporationRepository;
import de.digitalcollections.model.api.identifiable.entity.Corporation;
import java.util.Locale;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class ExternalCorporationRepositoryImpl implements ExternalCorporationRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExternalCorporationRepositoryImpl.class);

  LobidCorporationsClient client;

  @Value("${cudami.defaults.language}")
  private String defaultLanguage;

  @Value("${cudami.defaults.locale}")
  private Locale defaultLocale;

  @Override
  public Corporation getByGndId(String gndId) {
    try {
      Corporation corporation = client.getByGndId(gndId);
    } catch (HttpException ex) {
      java.util.logging.Logger.getLogger(ExternalCorporationRepositoryImpl.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

}
