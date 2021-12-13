package de.digitalcollections.cudami.server.backend.inmemory;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.LocaleRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class LocaleRepositoryImpl implements LocaleRepository {

  private CudamiConfig cudamiConfig;

  @Autowired
  public LocaleRepositoryImpl(CudamiConfig cudamiConfig) {
    this.cudamiConfig = cudamiConfig;
  }

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
    return cudamiConfig.getDefaults().getLanguage();
  }

  @Override
  public Locale getDefaultLocale() {
    return cudamiConfig.getDefaults().getLocale();
  }
}
