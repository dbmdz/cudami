package de.digitalcollections.cudami.client.feign.backend;

import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import feign.Param;
import feign.RequestLine;
import java.util.List;
import java.util.Locale;

public interface CudamiBackend extends CommonCudamiBackend {

  @RequestLine("GET /v2/languages/default")
  String getDefaultLanguage();

  @RequestLine("GET /v2/locales/default")
  Locale getDefaultLocale();

  @RequestLine("GET /v2/languages")
  List<String> getSupportedLanguages();

  @RequestLine("GET /v2/locales")
  List<Locale> getSupportedLocales();

  @RequestLine("GET /v2/webpages/{uuid}")
  Webpage getWebpage(@Param("uuid") String uuid);

  @RequestLine("GET /v2/webpages/{uuid}?pLocale={locale}")
  Webpage getWebpage(@Param("locale") Locale locale, @Param("uuid") String uuid);

  @RequestLine("GET /V2/websites/{uuid}")
  Website getWebsite(@Param("uuid") String uuid);

}
