package de.digitalcollections.cudami.template.website.springboot.repository;

import java.util.Locale;
import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class LocaleRepositoryImpl implements LocaleRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocaleRepositoryImpl.class);

  @Autowired
  private LocaleRepositoryEndpoint endpoint;

  @Override
  public Locale getDefault() {
    String defaultLocaleCode = endpoint.getDefault();
    return LocaleUtils.toLocale(defaultLocaleCode);
  }
}
