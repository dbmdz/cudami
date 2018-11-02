package de.digitalcollections.cudami.admin.backend.api.repository.identifiable;

import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.IdentifiablesContainer;
import java.util.List;

public interface IdentifiablesContainerRepository<IC extends IdentifiablesContainer, I extends Identifiable> {

  List<Identifiable> getIdentifiables(IC identifiablesContainer);

  List<Identifiable> saveIdentifiables(IC identifiablesContainer, List<Identifiable> identifiables);
}
