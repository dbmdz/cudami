package de.digitalcollections.cudami.client.feign.backend;

import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import feign.Param;
import feign.RequestLine;
import java.util.List;
import java.util.Locale;

public interface CudamiBackend extends CommonCudamiBackend {

  @RequestLine("GET /latest/locales")
  List<Locale> getAllLocales();

  @RequestLine("GET /latest/locales/default")
  Locale getDefaultLocale();

  @RequestLine("GET /latest/webpages/{uuid}")
  Webpage getWebpage(@Param("uuid") String uuid);

  @RequestLine("GET /latest/webpages/{uuid}?pLocale={locale}")
  Webpage getWebpage(@Param("locale") Locale locale, @Param("uuid") String uuid);

  @RequestLine("GET /latest/websites/{uuid}")
  Website getWebsite(@Param("uuid") String uuid);

}
