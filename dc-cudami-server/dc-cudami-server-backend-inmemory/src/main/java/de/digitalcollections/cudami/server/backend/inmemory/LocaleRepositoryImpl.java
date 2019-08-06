package de.digitalcollections.cudami.server.backend.inmemory;

import de.digitalcollections.cudami.server.backend.api.repository.LocaleRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class LocaleRepositoryImpl implements LocaleRepository {

  @Value("${cudami.defaults.language}")
  private String defaultLanguage;

  @Value("${cudami.defaults.locale}")
  private Locale defaultLocale;

  @Override
  public List<String> findAllLanguages() {
    return Arrays.asList(Locale.getISOLanguages());
  }

  @Override
  public List<Locale> findAllLocales() {
    return Arrays.asList(Locale.getAvailableLocales());
  }

  @Override
  public String getDefaultLanguage() {
    return defaultLanguage;
  }

  @Override
  public Locale getDefaultLocale() {
    return defaultLocale;
  }
}
