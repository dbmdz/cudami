package de.digitalcollections.model.jackson.mixin.identifiable.entity.item;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.EntityMixIn;
import de.digitalcollections.model.text.LocalizedText;

@JsonDeserialize(as = Item.class)
@JsonTypeName("ITEM")
public interface ItemMixIn extends EntityMixIn {

  @JsonIgnore
  LocalizedText getTitle();
}
