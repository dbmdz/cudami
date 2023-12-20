package de.digitalcollections.model.jackson.mixin.identifiable.web;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.Node;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.jackson.mixin.identifiable.IdentifiableMixIn;

@JsonDeserialize(as = Webpage.class)
@JsonTypeName("webpage")
public interface WebpageMixIn extends IdentifiableMixIn {

  @JsonIgnore
  Node<Webpage> getNode();
}
