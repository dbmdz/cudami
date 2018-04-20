package de.digitalcollections.cudami.model.impl.identifiable;

import de.digitalcollections.cudami.model.api.identifiable.Identifiable;
import de.digitalcollections.cudami.model.api.identifiable.IdentifiableType;
import de.digitalcollections.cudami.model.api.identifiable.parts.Text;
import de.digitalcollections.cudami.model.api.identifiable.resource.IiifImage;
import de.digitalcollections.cudami.model.api.identifiable.resource.MultilanguageDocument;
import java.time.LocalDateTime;
import java.util.UUID;

public class IdentifiableImpl implements Identifiable {

  private UUID uuid;
  protected LocalDateTime created;
  protected MultilanguageDocument description;
  protected Text label;
  protected LocalDateTime lastModified;
  protected IiifImage iiifImage;
  protected IdentifiableType type;

  @Override
  public UUID getUuid() {
    return uuid;
  }

  @Override
  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  @Override
  public LocalDateTime getCreated() {
    return created;
  }

  @Override
  public MultilanguageDocument getDescription() {
    return description;
  }

  @Override
  public Text getLabel() {
    return label;
  }

  @Override
  public LocalDateTime getLastModified() {
    return lastModified;
  }

  @Override
  public IiifImage getIiifImage() {
    return iiifImage;
  }

  @Override
  public void setCreated(LocalDateTime created) {
    this.created = created;
  }

  @Override
  public void setDescription(MultilanguageDocument description) {
    this.description = description;
  }

  @Override
  public void setLabel(Text label) {
    this.label = label;
  }

  @Override
  public void setLastModified(LocalDateTime lastModified) {
    this.lastModified = lastModified;
  }

  @Override
  public void setIiifImage(IiifImage iiifImage) {
    this.iiifImage = iiifImage;
  }

  @Override
  public IdentifiableType getType() {
    return this.type;
  }

  @Override
  public void setType(IdentifiableType identifiableType) {
    this.type = identifiableType;
  }

}
