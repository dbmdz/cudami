package de.digitalcollections.cudami.server.model;

import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.text.LocalizedText;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.LocaleUtils;

public class WebpageBuilder {

  Webpage webpage = new Webpage();

  public Webpage build() {
    return webpage;
  }

  public WebpageBuilder setCreated(String created) {
    webpage.setCreated(LocalDateTime.parse(created));
    return this;
  }

  public WebpageBuilder setLastModified(String lastModified) {
    webpage.setLastModified(LocalDateTime.parse(lastModified));
    return this;
  }

  public WebpageBuilder setIdentifiers(Set<Identifier> identifiers) {
    webpage.setIdentifiers(identifiers);
    return this;
  }

  public WebpageBuilder setLabel(Map<String, String> localizedContentMap) {
    LocalizedText label = new LocalizedText();
    for (Map.Entry<String, String> entry : localizedContentMap.entrySet()) {
      label.setText(LocaleUtils.toLocale(entry.getKey()), entry.getValue());
    }
    webpage.setLabel(label);
    return this;
  }

  public WebpageBuilder setUUID(String uuidStr) {
    webpage.setUuid(UUID.fromString(uuidStr));
    return this;
  }

  public WebpageBuilder setPublicationStart(String publicationStart) {
    webpage.setPublicationStart(LocalDate.parse(publicationStart));
    return this;
  }
}
