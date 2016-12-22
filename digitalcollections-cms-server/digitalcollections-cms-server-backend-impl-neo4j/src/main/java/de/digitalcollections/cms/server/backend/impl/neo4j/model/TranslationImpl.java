package de.digitalcollections.cms.server.backend.impl.neo4j.model;

import de.digitalcollections.cms.model.api.Translation;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity(label = "Translation")
public class TranslationImpl extends EntityImpl implements Translation {

  private String text;
  private String lang;

  private TranslationImpl() {
  }

  public TranslationImpl(String text, String lang) {
    if (StringUtils.isBlank(lang)) {
      throw new IllegalArgumentException("Language must not be null or empty!");
    }
    this.text = text;
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
  public String getLang() {
    return lang;
  }

  @Override
  public void setLang(String lang) {
    if (StringUtils.isBlank(lang)) {
      throw new IllegalArgumentException("Language must not be null or empty!");
    }
    this.lang = lang;
  }

  public boolean has(String lang) {
    return Objects.equals(this.lang, lang);
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
  public int hashCode() {
    return Objects.hash(text, lang);
  }

  @Override
  public String toString() {
    return "Translation{" + "graphId: " + getGraphId() + "', lang: '" + lang + "', text: '" + text + '}';
  }

}
