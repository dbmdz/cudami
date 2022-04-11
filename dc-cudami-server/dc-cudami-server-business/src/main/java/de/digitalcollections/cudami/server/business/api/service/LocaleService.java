package de.digitalcollections.cudami.server.business.api.service;

import java.util.List;
import java.util.Locale;

/** Service for Locales and languages. */
public interface LocaleService {

  /**
   * @return default language of content managed in cudami (ISO-2-code, e.g. "de", "en")
   */
  String getDefaultLanguage();

  /**
   * @return default locale of content managed in cudami
   */
  Locale getDefaultLocale();

  /**
   * @return list of all languages supported by cudami
   */
  List<String> getSupportedLanguages();

  /**
   * @return list of all locales supported by cudami
   */
  List<Locale> getSupportedLocales();
}
