package de.digitalcollections.cudami.client.rest.api;

import java.util.List;
import java.util.Locale;

public interface CudamiClient extends Client {

  List<Locale> getAllLocales() throws Exception;

  Locale getDefaultLocale() throws Exception;

}
