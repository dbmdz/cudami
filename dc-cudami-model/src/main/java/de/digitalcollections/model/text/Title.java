package de.digitalcollections.model.text;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

@SuperBuilder(buildMethodName = "prebuild")
public class Title implements Comparable<Title> {

  private LocalizedText text;
  private Set<Locale> textLocalesOfOriginalScripts;
  private TitleType titleType;

  public Title() {
    init();
  }

  public Title(LocalizedText text, Set<Locale> textLocalesOfOriginalScripts, TitleType titleType) {
    this();
    this.text = text;
    if (textLocalesOfOriginalScripts != null)
      this.textLocalesOfOriginalScripts = textLocalesOfOriginalScripts;
    this.titleType = titleType;
  }

  protected void init() {
    if (textLocalesOfOriginalScripts == null) textLocalesOfOriginalScripts = new HashSet<>(0);
  }

  public void addTextLocaleOfOriginalScript(Locale locale) {
    textLocalesOfOriginalScripts.add(locale);
  }

  public LocalizedText getText() {
    return text;
  }

  public Set<Locale> getTextLocalesOfOriginalScripts() {
    return textLocalesOfOriginalScripts;
  }

  public TitleType getTitleType() {
    return titleType;
  }

  public void setText(LocalizedText text) {
    this.text = text;
  }

  public void setTextLocalesOfOriginalScripts(Set<Locale> localesOfOriginalScripts) {
    this.textLocalesOfOriginalScripts = localesOfOriginalScripts;
  }

  public void setTitleType(TitleType titleType) {
    this.titleType = titleType;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || !(o instanceof Title)) {
      return false;
    }
    Title title = (Title) o;
    return this == title
        || Objects.equals(text, title.text)
            && Objects.equals(textLocalesOfOriginalScripts, title.textLocalesOfOriginalScripts)
            && Objects.equals(titleType, title.titleType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(text, textLocalesOfOriginalScripts, titleType);
  }

  @Override
  public int compareTo(Title title) {
    return 1000
            * StringUtils.compare(
                (titleType != null ? titleType.getMainType() : null),
                (title.getTitleType() != null ? title.getTitleType().getMainType() : null))
        + StringUtils.compare(
            (titleType != null ? titleType.getSubType() : null),
            (title.getTitleType() != null ? title.getTitleType().getSubType() : null));
  }

  @Override
  public String toString() {
    return "Title{"
        + "text="
        + text
        + ", textLocalesOfOriginalScripts="
        + textLocalesOfOriginalScripts
        + ", titleType="
        + titleType
        + '}';
  }

  public abstract static class TitleBuilder<C extends Title, B extends TitleBuilder<C, B>> {

    public C build() {
      C c = prebuild();
      c.init();
      return c;
    }

    public B textLocaleOfOriginalScript(Locale locale) {
      if (locale == null) {
        return self();
      }
      if (textLocalesOfOriginalScripts == null) {
        textLocalesOfOriginalScripts = new HashSet<>(1);
      }
      textLocalesOfOriginalScripts.add(locale);
      return self();
    }
  }
}
