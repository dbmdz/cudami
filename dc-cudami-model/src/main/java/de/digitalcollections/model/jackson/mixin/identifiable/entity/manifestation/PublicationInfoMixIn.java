package de.digitalcollections.model.jackson.mixin.identifiable.entity.manifestation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.entity.manifestation.PublicationInfo;

@JsonDeserialize(as = PublicationInfo.class)
@JsonTypeName("PUBLICATION_INFO")
public interface PublicationInfoMixIn {

  @JsonIgnore
  public boolean isEmpty();
}
