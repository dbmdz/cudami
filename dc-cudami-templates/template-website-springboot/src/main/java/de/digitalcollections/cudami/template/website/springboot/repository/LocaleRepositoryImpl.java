package de.digitalcollections.cudami.template.website.springboot.repository;

import de.digitalcollections.cudami.client.spring.business.CudamiException;
import de.digitalcollections.cudami.client.spring.business.CudamiService;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class LocaleRepositoryImpl implements LocaleRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocaleRepositoryImpl.class);

  @Autowired
  private CudamiService cudamiService;

  @Override
  public Locale getDefault() {
    try {
      return cudamiService.getDefaultLocale();
    } catch (CudamiException e) {
      LOGGER.error("Cannot get default locale: " + e, e);
      return null;
    }
  }

  @Override
  public List<Locale> getAll() {
    try {
      return cudamiService.getAllLocales();
    } catch (CudamiException e) {
      LOGGER.error("Cannot get all locales: " + e, e);
      return null;
    }
  }
}
