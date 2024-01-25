package de.digitalcollections.model.jackson.mixin.identifiable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.Node;

@JsonDeserialize(as = Node.class)
public interface NodeMixIn extends IdentifiableMixIn {}
