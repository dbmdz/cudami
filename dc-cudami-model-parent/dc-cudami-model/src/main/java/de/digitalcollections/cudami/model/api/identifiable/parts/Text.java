package de.digitalcollections.cudami.model.api.identifiable.parts;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

public interface Text {

  /**
   *
   * @return all locales for which translated texts are available.
   */
  Collection<Locale> getLocales();

  /**
   * @return first found text
   */
  String getText();

  /**
   * @param locale the desired locale.
   * @return text with the given locale
   */
  String getText(Locale locale);

  Set<Translation> getTranslations();

  void setTranslations(Set<Translation> translations);

  /**
   * sets (means: "add" or "replace") a text with a given locale
   *
   * @param locale the locale of <code>text</code>
   * @param text the text content
   */
  void setText(Locale locale, String text);

}
