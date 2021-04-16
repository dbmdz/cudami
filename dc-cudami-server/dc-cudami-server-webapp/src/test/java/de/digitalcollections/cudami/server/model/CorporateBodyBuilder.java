package de.digitalcollections.cudami.server.model;

import de.digitalcollections.model.identifiable.entity.EntityType;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import java.net.MalformedURLException;
import java.net.URL;

public class CorporateBodyBuilder extends EntityBuilder<CorporateBody, CorporateBodyBuilder> {

  @Override
  protected CorporateBody createEntity() {
    return new CorporateBody();
  }

  @Override
  protected EntityType getEntityType() {
    return EntityType.CORPORATE_BODY;
  }

  public CorporateBodyBuilder withHomepageUrl(String homepageUrl) {
    try {
      entity.setHomepageUrl(new URL(homepageUrl));
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    return this;
  }
}
