package de.digitalcollections.cudami.server.backend.api.repository.identifiable;

import de.digitalcollections.model.api.identifiable.Identifiable;
import java.util.List;
import java.util.UUID;

public interface IdentifiablesContainerRepository<I extends Identifiable> {

  List<Identifiable> getIdentifiables(I identifiable);
  
  List<Identifiable> getIdentifiables(UUID identifiableUuid);

//  void setIdentifiables(I identifiable, List<Identifiable> identifiables);

}
