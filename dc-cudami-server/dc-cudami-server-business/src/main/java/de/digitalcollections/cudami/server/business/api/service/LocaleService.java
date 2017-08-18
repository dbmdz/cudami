package de.digitalcollections.cudami.server.business.api.service;

import java.util.List;
import java.util.Locale;

/**
 * Service for Locales.
 */
public interface LocaleService {

  public List<Locale> getAll();

  public Locale getDefault();
}
