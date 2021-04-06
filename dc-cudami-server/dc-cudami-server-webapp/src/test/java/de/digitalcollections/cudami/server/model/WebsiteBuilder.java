package de.digitalcollections.cudami.server.model;

import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.text.contentblock.ContentBlock;
import de.digitalcollections.model.text.contentblock.Paragraph;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.LocaleUtils;
import org.flywaydb.core.internal.util.StringUtils;

public class WebsiteBuilder {

  Website website = new Website();

  public Website build() {
    return website;
  }

  public WebsiteBuilder setCreated(String created) {
    website.setCreated(LocalDateTime.parse(created));
    return this;
  }

  public WebsiteBuilder setSimpleDescription(Map<Locale, String> localizedContentMap) {
    LocalizedStructuredContent description = new LocalizedStructuredContent();
    for (Map.Entry<Locale, String> entry : localizedContentMap.entrySet()) {
      StructuredContent localizedDescription = new StructuredContent();
      String text = entry.getValue();
      ContentBlock paragraph = StringUtils.hasText(text) ? new Paragraph(text) : new Paragraph();
      localizedDescription.addContentBlock(paragraph);
      description.put(entry.getKey(), localizedDescription);
    }

    website.setDescription(description);
    return this;
  }

  public WebsiteBuilder setLabel(Map<String, String> localizedContentMap) {
    LocalizedText label = new LocalizedText();
    for (Map.Entry<String, String> entry : localizedContentMap.entrySet()) {
      label.setText(LocaleUtils.toLocale(entry.getKey()), entry.getValue());
    }
    website.setLabel(label);
    return this;
  }

  public WebsiteBuilder setLastModified(String lastModified) {
    website.setLastModified(LocalDateTime.parse(lastModified));
    return this;
  }

  public WebsiteBuilder setUuid(String uuidStr) {
    website.setUuid(UUID.fromString(uuidStr));
    return this;
  }

  public WebsiteBuilder setRefId(long refId) {
    website.setRefId(refId);
    return this;
  }

  public WebsiteBuilder setUrl(String url) throws MalformedURLException {
    website.setUrl(new URL(url));
    return this;
  }
}
