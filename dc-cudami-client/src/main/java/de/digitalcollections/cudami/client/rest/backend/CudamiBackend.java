package de.digitalcollections.cudami.client.rest.backend;

import de.digitalcollections.cudami.model.api.identifiable.entity.Website;
import feign.Param;
import feign.RequestLine;
import java.util.List;
import java.util.Locale;

public interface CudamiBackend extends CommonCudamiBackend {

  @RequestLine("GET /v1/locales")
  List<Locale> getAllLocales();

  @RequestLine("GET /v1/locales/default")
  Locale getDefaultLocale();

  @RequestLine("GET /v1/webpages/{uuid}")
  String getWebpage(@Param("uuid") String uuid);

  @RequestLine("GET /v1/websites/{uuid}")
  Website getWebsite(@Param("uuid") String uuid);

}
