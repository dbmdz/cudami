package de.digitalcollections.cudami.client.business.api.service;

import java.util.List;
import java.util.Locale;

/**
 * Service for Locales.
 */
public interface LocaleService {

  public List<Locale> findAll();

  public Locale getDefault();
}
