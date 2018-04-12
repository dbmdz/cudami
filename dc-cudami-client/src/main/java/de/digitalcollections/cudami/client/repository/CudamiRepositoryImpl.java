package de.digitalcollections.cudami.client.repository;

import de.digitalcollections.cudami.client.rest.api.CudamiClient;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CudamiRepositoryImpl implements CudamiRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(CudamiRepositoryImpl.class);

  @Autowired
  private CudamiClient cudamiClient;

  @Override
  public Locale getDefaultLocale() {
    try {
      return cudamiClient.getDefaultLocale();
    } catch (Exception e) {
      LOGGER.error("Cannot get default locale from Cudami: " + e, e);
    }

    return null;
  }

  @Override
  public List<Locale> getAllLocales() {
    try {
      return cudamiClient.getAllLocales();
    } catch (Exception e) {
      LOGGER.error("Cannot get all locales from Cudami: " + e, e);
    }

    return null;
  }
}
