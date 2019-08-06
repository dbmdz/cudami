package de.digitalcollections.cudami.admin.backend.impl.repository;

import feign.RequestLine;
import java.util.List;

public interface LocaleRepositoryEndpoint {

  @RequestLine("GET /latest/languages")
  List<String> findAllLanguages();

  @RequestLine("GET /latest/languages/default")
  String getDefaultLanguage();

  @RequestLine("GET /latest/locales")
  List<String> findAllLocales();

  @RequestLine("GET /latest/locales/default")
  String getDefaultLocale();
}
