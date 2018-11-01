package de.digitalcollections.cudami.admin.business.api.service.identifiable;

import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.IdentifiablesContainer;
import java.util.List;

public interface IdentifiablesContainerService<IC extends IdentifiablesContainer, I extends Identifiable> {

  List<Identifiable> getIdentifiables(IC identifiablesContainer);

  void saveIdentifiables(IC identifiablesContainer, List<Identifiable> identifiables);
}
