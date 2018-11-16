package de.digitalcollections.cudami.admin.backend.api.repository.identifiable;

import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.parts.Translation;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public interface IdentifiableRepository<I extends Identifiable> {

  long count();

  I create();

  PageResponse<I> find(PageRequest pageRequest);

  List<I> find(String searchTerm, int maxResults);

  I findOne(UUID uuid);

  default I findOne(UUID uuid, Locale locale) {
    I identifiable = findOne(uuid);
    // TODO maybe a better solution to just get locale specific fields directly from database instead of removing it here?
    identifiable.getLabel().getTranslations().removeIf((Translation translation) -> !translation.getLocale().equals(locale));
    identifiable.getDescription().getLocalizedStructuredContent().entrySet().removeIf((Map.Entry entry) -> !entry.getKey().equals(locale));
    return identifiable;
  }

  I save(I identifiable);

  I update(I identifiable);

}
