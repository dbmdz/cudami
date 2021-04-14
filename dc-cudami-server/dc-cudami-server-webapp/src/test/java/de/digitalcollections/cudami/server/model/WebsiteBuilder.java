package de.digitalcollections.cudami.server.model;

import de.digitalcollections.model.identifiable.entity.EntityType;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.text.contentblock.ContentBlock;
import de.digitalcollections.model.text.contentblock.Paragraph;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import org.flywaydb.core.internal.util.StringUtils;

public class WebsiteBuilder extends EntityBuilder<Website, WebsiteBuilder> {

  @Override
  protected Website createEntity() {
    return new Website();
  }

  @Override
  protected EntityType getEntityType() {
    return EntityType.WEBSITE;
  }

  @Deprecated
  public WebsiteBuilder withSimpleDescription(Locale locale, String text) {
    LocalizedStructuredContent description = entity.getDescription();
    if (description == null) {
      description = new LocalizedStructuredContent();
    }
    StructuredContent localizedDescription = description.get(locale);
    if (localizedDescription == null) {
      localizedDescription = new StructuredContent();
    }
    ContentBlock paragraph = StringUtils.hasText(text) ? new Paragraph(text) : new Paragraph();
    localizedDescription.addContentBlock(paragraph);
    description.put(locale, localizedDescription);
    entity.setDescription(description);
    return this;
  }

  public WebsiteBuilder withUrl(String url) throws MalformedURLException {
    entity.setUrl(new URL(url));
    return this;
  }

  public WebsiteBuilder withDescription(LocalizedStructuredContent description) {
    entity.setDescription(description);
    return this;
  }

  public WebsiteBuilder withRootPages(List<Webpage> rootPages) {
    entity.setRootPages(rootPages);
    return this;
  }
}
