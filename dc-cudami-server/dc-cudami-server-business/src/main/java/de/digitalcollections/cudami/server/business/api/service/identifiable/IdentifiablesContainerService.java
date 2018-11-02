package de.digitalcollections.cudami.server.business.api.service.identifiable;

import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.IdentifiablesContainer;
import java.util.List;
import java.util.UUID;

public interface IdentifiablesContainerService<IC extends IdentifiablesContainer, I extends Identifiable> {

  List<Identifiable> getIdentifiables(IC identifiablesContainer);

  List<Identifiable> getIdentifiables(UUID identifiablesContainerUuid);

  List<Identifiable> saveIdentifiables(IC identifiablesContainer, List<Identifiable> identifiables);
  
  List<Identifiable> saveIdentifiables(UUID identifiablesContainerUuid, List<Identifiable> identifiables);
}
