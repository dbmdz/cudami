package de.digitalcollections.cudami.server.model;

import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.text.contentblock.ContentBlock;
import de.digitalcollections.model.text.contentblock.Paragraph;
import java.util.Locale;
import org.apache.commons.lang3.LocaleUtils;

public class DescriptionBuilder {

  LocalizedStructuredContent description = new LocalizedStructuredContent();
  private Locale locale;
  private StructuredContent content;
  private ContentBlock contentBlock;

  public LocalizedStructuredContent build() {
    if (locale != null && content != null) {
      description.put(locale, content);
    }
    return description;
  }

  public DescriptionBuilder setType(String type) {
    if (content == null) {
      content = new StructuredContent();
    }
    if (contentBlock == null) {
      switch (type) {
        case "paragraph":
          contentBlock = new Paragraph();
          break;
      }
    }
    content.addContentBlock(contentBlock);
    return this;
  }

  public DescriptionBuilder setLanguage(String language) {
    this.locale = LocaleUtils.toLocale(language);
    this.content = new StructuredContent();
    return this;
  }
}
