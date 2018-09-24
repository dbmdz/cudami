package de.digitalcollections.cudami.admin.business.api.service.identifiable;

import de.digitalcollections.model.api.identifiable.Identifiable;
import java.util.List;

public interface IdentifiablesContainerService<I extends Identifiable> {
  List<Identifiable> getIdentifiables(I identifiable);

//  void setIdentifiables(I identifiable, List<Identifiable> identifiables);
  
  // I addIdentifiable(I identifiable, Identifiable);
}
