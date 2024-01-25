package de.digitalcollections.model.jackson.mixin.identifiable.entity.digitalobject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.EntityMixIn;
import java.util.LinkedHashSet;

@JsonDeserialize(as = DigitalObject.class)
@JsonTypeName("DIGITAL_OBJECT")
public interface DigitalObjectMixIn extends EntityMixIn {

  @JsonIgnore
  LinkedHashSet<FileResource> addFileResource(FileResource fileResource);
}
