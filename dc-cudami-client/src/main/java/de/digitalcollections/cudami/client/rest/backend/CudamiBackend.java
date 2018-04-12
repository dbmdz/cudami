package de.digitalcollections.cudami.client.rest.backend;

import feign.RequestLine;
import java.util.List;
import java.util.Locale;

public interface CudamiBackend extends CommonCudamiBackend {

  @RequestLine("GET /v1/locales")
  List<Locale> getAllLocales();

  @RequestLine("GET /v1/locales/default")
  Locale getDefaultLocale();

}
