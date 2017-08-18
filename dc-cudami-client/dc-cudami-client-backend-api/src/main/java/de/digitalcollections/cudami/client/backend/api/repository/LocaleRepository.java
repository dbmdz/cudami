package de.digitalcollections.cudami.client.backend.api.repository;

import java.util.List;
import java.util.Locale;

/**
 * Repository for Locale persistence handling.
 */
public interface LocaleRepository {

  public List<Locale> findAll();

  public Locale getDefault();
}
