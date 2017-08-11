package de.digitalcollections.cudami.model.api.entity;

import de.digitalcollections.iiif.presentation.model.api.enums.Version;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;

/**
 * IiifContent is used for IIIF content specified by an IIIF manifest.
 * @param <ID> unique serializable identifier
 */
public interface IiifContent<ID extends Serializable> extends Entity<ID> {

  URL getManifestUrl();

  void setManifesturl(URL manifestUrl);

  URL getLogoUrl();

  void setLogoUrl(URL logoUrl);

  default void addManifestLabel(Locale locale, String manifestLabel) {
    getManifestLabels().put(locale, manifestLabel);
  }

  default String getManifestLabel(String language) {
    return getManifestLabels().get(new Locale(language));
  }

  default String getManifestLabel(Locale locale) {
    return getManifestLabels().get(locale);
  }

  HashMap<Locale, String> getManifestLabels();

  void setManifestLabels(HashMap<Locale, String> manifestLabels);

  default void addDescription(Locale locale, String description) {
    getDescriptions().put(locale, description);
  }

  default String getDescription(String language) {
    return getDescriptions().get(new Locale(language));
  }

  default String getDescription(Locale locale) {
    return getDescriptions().get(locale);
  }

  HashMap<Locale, String> getDescriptions();

  void setDescriptions(HashMap<Locale, String> descriptions);

  default void addAttribution(Locale locale, String attribution) {
    getAttributions().put(locale, attribution);
  }

  default String getAttribution(String language) {
    return getAttributions().get(new Locale(language));
  }

  default String getAttribution(Locale locale) {
    return getAttributions().get(locale);
  }

  HashMap<Locale, String> getAttributions();

  void setAttributions(HashMap<Locale, String> attributions);

  Version getManifestVersion();

  void setManifestVersion(Version version);
}
