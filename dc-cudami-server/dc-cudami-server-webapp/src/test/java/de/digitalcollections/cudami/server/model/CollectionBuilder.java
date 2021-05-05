package de.digitalcollections.cudami.server.model;

import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.EntityType;
import java.time.LocalDate;

public class CollectionBuilder extends EntityBuilder<Collection, CollectionBuilder> {

  @Override
  protected Collection createEntity() {
    return new Collection();
  }

  @Override
  protected EntityType getEntityType() {
    return EntityType.COLLECTION;
  }

  public CollectionBuilder withPublicationStart(String publicationStart) {
    entity.setPublicationStart(LocalDate.parse(publicationStart));
    return this;
  }
}
