package de.digitalcollections.cudami.client.business.impl.service;

import de.digitalcollections.cudami.client.backend.api.repository.LocaleRepository;
import de.digitalcollections.cudami.client.business.api.service.LocaleService;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for Locales handling.
 */
@Service
public class LocaleServiceImpl implements LocaleService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocaleServiceImpl.class);

  @Autowired
  private LocaleRepository repository;

  @Override
  public List<Locale> findAll() {
    List<Locale> locales = repository.findAll();
    return locales;
  }

  @Override
  public Locale getDefault() {
    return repository.getDefault();
  }

}
