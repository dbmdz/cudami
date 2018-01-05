package de.digitalcollections.cudami.model.api.identifiable;

import de.digitalcollections.cudami.model.api.identifiable.parts.Text;
import java.time.LocalDateTime;
import java.util.UUID;
import de.digitalcollections.cudami.model.api.identifiable.resource.IiifImage;

public interface Identifiable {

  LocalDateTime getCreated();

  Text getDescription();

  Text getLabel();

  LocalDateTime getLastModified();

  IiifImage getIiifImage();
  
  IdentifiableType getType();

  UUID getUuid();

  void setCreated(LocalDateTime created);

  void setDescription(Text description);

  void setLabel(Text label);

  void setLastModified(LocalDateTime lastModified);

  void setIiifImage(IiifImage thumbnail);
  
  void setType(IdentifiableType identifiableType);

  void setUuid(UUID uuid);
}
