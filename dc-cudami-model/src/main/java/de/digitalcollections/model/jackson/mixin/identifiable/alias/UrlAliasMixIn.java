package de.digitalcollections.model.jackson.mixin.identifiable.alias;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.jackson.mixin.UniqueObjectMixIn;
import java.util.UUID;

@JsonDeserialize(as = UrlAlias.class)
@JsonTypeName("URL_ALIAS")
public interface UrlAliasMixIn extends UniqueObjectMixIn {
  @JsonIgnore
  public IdentifiableObjectType getTargetIdentifiableObjectType();

  @JsonIgnore
  public IdentifiableType getTargetIdentifiableType();

  @JsonIgnore
  public UUID getTargetUuid();
}
