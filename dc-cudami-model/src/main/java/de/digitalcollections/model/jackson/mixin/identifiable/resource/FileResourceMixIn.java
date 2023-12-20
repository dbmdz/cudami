package de.digitalcollections.model.jackson.mixin.identifiable.resource;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.digitalcollections.model.identifiable.resource.ApplicationFileResource;
import de.digitalcollections.model.identifiable.resource.AudioFileResource;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.identifiable.resource.TextFileResource;
import de.digitalcollections.model.identifiable.resource.VideoFileResource;
import de.digitalcollections.model.jackson.mixin.identifiable.IdentifiableMixIn;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "fileResourceType",
    visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = ApplicationFileResource.class, name = "APPLICATION"),
  @JsonSubTypes.Type(value = AudioFileResource.class, name = "AUDIO"),
  @JsonSubTypes.Type(value = FileResource.class, name = "UNDEFINED"),
  @JsonSubTypes.Type(value = ImageFileResource.class, name = "IMAGE"),
  @JsonSubTypes.Type(value = LinkedDataFileResource.class, name = "LINKED_DATA"),
  @JsonSubTypes.Type(value = TextFileResource.class, name = "TEXT"),
  @JsonSubTypes.Type(value = VideoFileResource.class, name = "VIDEO")
})
public interface FileResourceMixIn extends IdentifiableMixIn {}
