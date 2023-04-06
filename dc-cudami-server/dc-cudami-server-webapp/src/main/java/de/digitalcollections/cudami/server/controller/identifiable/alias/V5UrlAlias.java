package de.digitalcollections.cudami.server.controller.identifiable.alias;

import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import java.util.UUID;

public class V5UrlAlias extends UrlAlias {
  public V5UrlAlias() {
    super();
    super.setTarget(new Identifiable());
  }

  void setTargetIdentifiableType(IdentifiableType identifiableType) {
    super.getTarget().setType(identifiableType);
  }

  void setTargetIdentifiableObjectType(IdentifiableObjectType identifiableObjectType) {
    super.getTarget().setIdentifiableObjectType(identifiableObjectType);
  }

  void setTargetUuid(UUID uuid) {
    super.getTarget().setUuid(uuid);
  }
}
