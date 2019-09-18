package de.digitalcollections.cudami.admin.backend.impl.repository;

import feign.RequestLine;
import java.util.List;
import java.util.Locale;

public interface LocaleRepository {

  @RequestLine("GET /latest/languages")
  List<String> findAllLanguages();

  @RequestLine("GET /latest/languages/default")
  Locale getDefaultLanguage();

  @RequestLine("GET /latest/locales")
  List<String> findAllLocales();

  @RequestLine("GET /latest/locales/default")
  String getDefaultLocale();
}
