package de.digitalcollections.model.jackson.mixin.identifiable.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.Node;
import de.digitalcollections.model.identifiable.entity.Collection;

@JsonDeserialize(as = Collection.class)
@JsonTypeName("COLLECTION")
public interface CollectionMixIn extends EntityMixIn {

  @JsonIgnore
  Node<Collection> getNode();
}
