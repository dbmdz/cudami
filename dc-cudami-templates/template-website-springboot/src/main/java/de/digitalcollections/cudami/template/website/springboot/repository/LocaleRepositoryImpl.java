package de.digitalcollections.cudami.template.website.springboot.repository;

import de.digitalcollections.cudami.client.CudamiLocalesClient;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class LocaleRepositoryImpl implements LocaleRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocaleRepositoryImpl.class);

  @Autowired private CudamiLocalesClient cudamiLocalesClient;

  @Override
  public Locale getDefault() {
    try {
      return cudamiLocalesClient.getDefaultLanguage();
    } catch (Exception e) {
      LOGGER.error("Cannot get default locale: " + e, e);
      return null;
    }
  }

  @Override
  public List<Locale> getAll() {
    try {
      return cudamiLocalesClient.findAllLanguages().stream()
          .map(language -> new Locale(language))
          .collect(Collectors.toList());
    } catch (Exception e) {
      LOGGER.error("Cannot get all locales: " + e, e);
      return null;
    }
  }
}
