package de.digitalcollections.cudami.client.repository;

import java.util.List;
import java.util.Locale;

public interface CudamiRepository {

  public Locale getDefaultLocale();

  public List<Locale> getAllLocales();
}
