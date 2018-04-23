package de.digitalcollections.cudami.model.impl.identifiable.parts;

import de.digitalcollections.cudami.model.api.identifiable.parts.Translation;
import java.util.Locale;
import java.util.Objects;

public class TranslationImpl implements Translation {

  private Locale locale;
  private String text;

  private TranslationImpl() {
  }

  public TranslationImpl(Locale locale, String text) {
    if (locale == null) {
      throw new IllegalArgumentException("Locale must not be null!");
    }
    this.text = text;
    this.locale = locale;
  }

  public TranslationImpl(Translation translation) {
    this(translation.getLocale(), translation.getText());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof TranslationImpl) {
      TranslationImpl other = (TranslationImpl) obj;
      return Objects.equals(text, other.getText())
              && Objects.equals(locale, other.getLocale());
    }
    return false;
  }

  @Override
  public Locale getLocale() {
    return locale;
  }

  @Override
  public void setLocale(Locale locale) {
    if (locale == null) {
      throw new IllegalArgumentException("Locale must not be null!");
    }
    this.locale = locale;
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
  public boolean has(Locale locale) {
    return Objects.equals(this.locale, locale);
  }

  @Override
  public int hashCode() {
    return Objects.hash(text, locale);
  }

  @Override
  public String toString() {
    return "Translation {" + "locale: '" + locale + "', text: '" + text + '}';
  }

}
