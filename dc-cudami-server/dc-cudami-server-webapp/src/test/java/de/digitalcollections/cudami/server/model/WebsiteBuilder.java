package de.digitalcollections.cudami.server.model;

import de.digitalcollections.model.identifiable.entity.EntityType;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class WebsiteBuilder extends EntityBuilder<Website, WebsiteBuilder> {

  @Override
  protected Website createEntity() {
    return new Website();
  }

  @Override
  protected EntityType getEntityType() {
    return EntityType.WEBSITE;
  }

  public WebsiteBuilder withUrl(String url) throws MalformedURLException {
    entity.setUrl(new URL(url));
    return this;
  }

  @Deprecated
  public WebsiteBuilder withDescription(LocalizedStructuredContent description) {
    entity.setDescription(description);
    return this;
  }

  public WebsiteBuilder withRootPages(List<Webpage> rootPages) {
    entity.setRootPages(rootPages);
    return this;
  }
}
