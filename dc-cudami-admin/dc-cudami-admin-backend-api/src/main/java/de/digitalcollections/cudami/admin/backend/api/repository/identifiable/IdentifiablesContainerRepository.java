package de.digitalcollections.cudami.admin.backend.api.repository.identifiable;

import de.digitalcollections.model.api.identifiable.Identifiable;
import java.util.List;

public interface IdentifiablesContainerRepository<I extends Identifiable> {

  List<Identifiable> getIdentifiables(I identifiable);

//  void setIdentifiables(I identifiable, List<Identifiable> identifiables);

}
