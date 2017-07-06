package de.digitalcollections.cudami.server.backend.impl.neo4j.model;

import com.google.gson.Gson;
import de.digitalcollections.cudami.model.api.Text;
import static de.digitalcollections.cudami.model.api.Text.DEFAULT_LANG;
import de.digitalcollections.cudami.model.api.Translation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity(label = "Text")
public class TextImpl implements Text {

  public static Text copyText(Text text) {
    if (text != null) {
      return new TextImpl(text);
    }
    return null;
  }

  public static List<Text> copyTexts(List<Text> texts) {
    List<Text> copies = new ArrayList<>(texts);
    copies.replaceAll(text -> new TextImpl(text));
    return copies;
  }

  @GraphId
  private Long graphId;

  @Relationship(type = "HAS_TRANSLATION")
  private Set<Translation> translations;

  private TextImpl() {
    translations = new HashSet<>();
  }

  public TextImpl(Text text) {
    translations = new HashSet<>();
    for (Translation translation : text.getTranslations()) {
      translations.add(new TranslationImpl(translation.getText(), translation.getLang()));
    }
  }

  public TextImpl(String text) {
    this(DEFAULT_LANG, text);
  }

  public TextImpl(String lang, String text) {
    translations = new HashSet<>();
    translations.add(new TranslationImpl(text, lang));
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof Text) {
      Text other = (Text) obj;
      if (translations.size() == other.getLanguages().size()) {
        boolean allAreEqual = true;
        for (Translation translation : translations) {
          allAreEqual = allAreEqual && Objects.equals(translation.getText(), other.getText(translation.getLang()));
        }
        return allAreEqual;
      }
    }
    return false;
  }

  private Translation findTranslation(String lang) {
    for (Translation translation : translations) {
      if (translation.has(lang)) {
        return translation;
      }
    }
    return null;
  }

  public Long getGraphId() {
    return graphId;
  }

  public void setGraphId(Long graphId) {
    this.graphId = graphId;
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
    return getText(DEFAULT_LANG);
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
  public void setText(String text) {
    setText(DEFAULT_LANG, text);
  }

  @Override
  public Set<Translation> getTranslations() {
    return translations;
  }

  @Override
  public void setTranslations(Set<Translation> translations) {
    this.translations = translations;
  }

  @Override
  public int hashCode() {
    Collection<Object> items = new ArrayList<>();
    for (Translation translation : translations) {
      items.add(translation.getLang());
      items.add(translation.getText());
    }
    return Objects.hash(items.toArray());
  }

  @Override
  public void setText(String lang, String text) {
    Translation translation = findTranslation(lang);
    if (translation != null) {
      translation.setText(text);
    } else {
      translations.add(new TranslationImpl(text, lang));
    }
  }

  @Override
  public String toString() {
    Gson gson = new Gson();
    String json = gson.toJson(this);
    return json;
  }
}
