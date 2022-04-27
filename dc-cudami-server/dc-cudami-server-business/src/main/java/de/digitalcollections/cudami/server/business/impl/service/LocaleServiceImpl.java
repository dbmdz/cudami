package de.digitalcollections.cudami.server.business.impl.service;

import de.digitalcollections.cudami.server.backend.api.repository.LocaleRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Service for Locale handling. */
@Service
public class LocaleServiceImpl implements LocaleService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocaleServiceImpl.class);

  @Autowired private LocaleRepository repository;

  @Override
  public String getDefaultLanguage() {
    return repository.getDefaultLanguage();
  }

  @Override
  public Locale getDefaultLocale() {
    return repository.getDefaultLocale();
  }

  @Override
  public List<String> getSupportedLanguages() {
    return repository.getSupportedLanguages();
  }

  @Override
  public List<Locale> getSupportedLocales() {
    return repository.getSupportedLocales();
  }
}
