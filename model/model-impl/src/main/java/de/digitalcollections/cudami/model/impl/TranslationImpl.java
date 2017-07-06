package de.digitalcollections.cudami.model.impl;

import com.google.common.base.Strings;
import de.digitalcollections.cudami.model.api.Translation;
import java.util.Objects;

public class TranslationImpl implements Translation {

  private String lang;
  private String text;

  private TranslationImpl() {
  }

  public TranslationImpl(String text, String lang) {
    if (Strings.isNullOrEmpty(lang)) {
      throw new IllegalArgumentException("Language must not be null or empty!");
    }
    this.text = text;
    this.lang = lang;
  }

  public TranslationImpl(Translation translation) {
    this(translation.getText(), translation.getLang());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof TranslationImpl) {
      TranslationImpl other = (TranslationImpl) obj;
      return Objects.equals(text, other.getText())
          && Objects.equals(lang, other.getLang());
    }
    return false;
  }

  @Override
  public String getLang() {
    return lang;
  }

  @Override
  public void setLang(String lang) {
    if (Strings.isNullOrEmpty(lang)) {
      throw new IllegalArgumentException("Language must not be null or empty!");
    }
    this.lang = lang;
  }

  @Override
  public String getText() {
    return text;
  }

  @Override
  public void setText(String text) {
    this.text = text;
  }

  @Override
  public boolean has(String lang) {
    return Objects.equals(this.lang, lang);
  }

  @Override
  public int hashCode() {
    return Objects.hash(text, lang);
  }

  @Override
  public String toString() {
    return "Translation{" + "lang: '" + lang + "', text: '" + text + '}';
  }

}
