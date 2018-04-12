package de.digitalcollections.cudami.client.business;

import java.util.List;
import java.util.Locale;

public interface CudamiService {

  public Locale getDefaultLocale();

  public List<Locale> getAllLocales();

}
