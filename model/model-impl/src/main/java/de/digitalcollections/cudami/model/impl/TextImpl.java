package de.digitalcollections.cudami.model.impl;

import de.digitalcollections.cudami.model.api.Text;
import de.digitalcollections.cudami.model.api.Translation;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TextImpl implements Text {

  private Set<Translation> translations;

  public TextImpl() {
    translations = new HashSet<>();
  }

  public TextImpl(String lang, String text) {
    this();
    translations.add(new TranslationImpl(text, lang));
  }

  public TextImpl(List<String> languages, String text) {
    this();
    for (String lang : languages) {
      translations.add(new TranslationImpl(text, lang));
    }
  }

  public TextImpl(Text text) {
    this();
    for (Translation translation : text.getTranslations()) {
      translations.add(new TranslationImpl(translation.getText(), translation.getLang()));
    }
  }

  private Translation findTranslation(String lang) {
    for (Translation translation : translations) {
      if (translation.has(lang)) {
        return translation;
      }
    }
    return null;
  }

  @Override
  public Collection<String> getLanguages() {
    Set<String> languages = new HashSet<>();
    for (Translation translation : translations) {
      languages.add(translation.getLang());
    }
    return languages;
  }

  @Override
  public String getText() {
    Collection<String> langs = getLanguages();
    if (langs.isEmpty()) {
      return null;
    }
    if (langs.size() == 1) {
      return getText(langs.iterator().next());
    }
    if (langs.contains(DEFAULT_LANG)) {
      return getText(DEFAULT_LANG);
    } else {
      return getText(langs.iterator().next());
    }
  }

  @Override
  public void setText(String text) {
    Translation translation = findTranslation(DEFAULT_LANG);
    if (translation != null) {
      translation.setText(text);
    } else {
      translations.add(new TranslationImpl(text, DEFAULT_LANG));
    }
  }

  @Override
  public String getText(String lang) {
    Translation translation = findTranslation(lang);
    if (translation != null) {
      return translation.getText();
    }
    return null;
  }

  @Override
  public Set<Translation> getTranslations() {
    return translations;
  }

  @Override
  public void setTranslations(Set<Translation> translations
  ) {
    this.translations = translations;
  }

  @Override
  public void setText(String lang, String text
  ) {
    Translation translation = findTranslation(lang);
    if (translation != null) {
      translation.setText(text);
    } else {
      translations.add(new TranslationImpl(text, lang));
    }
  }

}
