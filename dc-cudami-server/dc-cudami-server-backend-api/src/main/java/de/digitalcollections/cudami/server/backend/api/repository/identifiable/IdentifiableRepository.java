package de.digitalcollections.cudami.server.backend.api.repository.identifiable;

import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.cudami.model.api.identifiable.Identifiable;
import de.digitalcollections.cudami.model.api.identifiable.parts.Translation;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public interface IdentifiableRepository<I extends Identifiable> {

  long count();

  I create();

  PageResponse<I> find(PageRequest pageRequest);

  I findOne(UUID uuid);

  default I findOne(UUID uuid, Locale locale) {
    I identifiable = findOne(uuid);
    // TODO maybe a better solution to just get locale specific fields directly from database instead of removing it here?
    identifiable.getLabel().getTranslations().removeIf((Translation translation) -> !translation.getLocale().equals(locale));
    identifiable.getDescription().getDocuments().entrySet().removeIf((Map.Entry entry) -> !entry.getKey().equals(locale));
    return identifiable;
  }

  I save(I identifiable);

  I update(I identifiable);

}
