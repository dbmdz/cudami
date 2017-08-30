package de.digitalcollections.cudami.model.api;

import java.util.Collection;
import java.util.Set;

public interface Text {

  public static final String DEFAULT_LANG = "de";

  /**
   *
   * @return all languages for which translated texts are available.
   */
  Collection<String> getLanguages();

  /**
   * @return text with default lang ("de")
   */
  String getText();

  /**
   * @param lang the desired language.
   * @return text with the given lang
   */
  String getText(String lang);

  /**
   * sets (means: "add" or "replace") a text with the default locale ("de")
   *
   * @param text the text for default locale.
   */
  void setText(String text);

  /**
   * sets (means: "add" or "replace") a text with a given locale, which is calculated out of the lang string
   *
   * @param lang the language of <code>text</code>
   * @param text the text content
   */
  void setText(String lang, String text);

  Set<Translation> getTranslations();

  void setTranslations(Set<Translation> translations);

}
