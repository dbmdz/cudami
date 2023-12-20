package de.digitalcollections.model.legal;

import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.text.LocalizedText;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Objects;
import lombok.experimental.SuperBuilder;

/**
 * License model/description containing all relevant metadata of a license that can be used to
 * specify the license under which an object is available or distributed.
 */
@SuperBuilder(buildMethodName = "prebuild")
public class License extends UniqueObject {

  private String acronym;
  private LocalizedText label;
  private URL url;

  public License() {
    super();
  }

  public License(String acronym, LocalizedText label, URL url) {
    this();
    this.acronym = acronym;
    this.label = label;
    this.url = url;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof License)) {
      return false;
    }
    License license = (License) o;
    return Objects.equals(acronym, license.acronym)
        && Objects.equals(label, license.label)
        && Objects.equals(url, license.url)
        && Objects.equals(uuid, license.uuid);
  }

  /**
   * @return the (language independent/english) acronym for this license, e.g. "CC0 1.0" for "Public
   *     Domain" or "CC BY-NC-SA 3.0"
   */
  public String getAcronym() {
    return acronym;
  }

  /**
   * @return multilingual license title, e.g. "URHEBERRECHTSSCHUTZ - NICHT KOMMERZIELLE NUTZUNG
   *     GESTATTET", "IN COPYRIGHT - NON-COMMERCIAL USE PERMITTED"
   */
  public LocalizedText getLabel() {
    return label;
  }

  /**
   * @return the url to the official public accessible documentation/specification of the license,
   *     e.g. "http://rightsstatements.org/vocab/InC-NC/1.0/"
   */
  public URL getUrl() {
    return url;
  }

  @Override
  public int hashCode() {
    return Objects.hash(acronym, label, url, uuid);
  }

  public void setAcronym(String acronym) {
    this.acronym = acronym;
  }

  public void setLabel(LocalizedText label) {
    this.label = label;
  }

  public void setUrl(URL url) {
    this.url = url;
  }

  @Override
  public String toString() {
    return "License{"
        + "created="
        + created
        + ", lastModified="
        + lastModified
        + ", uuid="
        + uuid
        + ", acronym='"
        + acronym
        + '\''
        + ", label="
        + label
        + ", url="
        + url
        + '}';
  }

  public abstract static class LicenseBuilder<C extends License, B extends LicenseBuilder<C, B>>
      extends UniqueObjectBuilder<C, B> {

    @Override
    public C build() {
      C c = prebuild();
      c.init();
      return c;
    }

    public B label(Locale locale, String localizedLabel) {
      if (label == null) {
        label = new LocalizedText();
      }
      label.setText(locale, localizedLabel);
      return self();
    }

    public B url(String url) {
      if (url == null) {
        return self();
      }

      try {
        this.url = new URL(url);
      } catch (MalformedURLException e) {
        throw new RuntimeException("Cannot set url=" + url + ": " + e, e);
      }
      return self();
    }
  }
}
