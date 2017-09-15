package de.digitalcollections.cudami.template.website.springboot.repository;

import java.util.Locale;

/**
 * Repository for Locale persistence handling.
 */
public interface LocaleRepository {

  public Locale getDefault();
}
