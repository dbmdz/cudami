package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.semantic;

import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.semantic.Headword;
import java.util.UUID;

public interface EntityHeadwordRepository {

  default void add(Entity entity, Headword headword) {
    add(entity.getUuid(), headword.getUuid());
  }

  void add(UUID entityUuid, UUID headwordUuid);

  /**
   * Get paged, sorted, filtered entities
   *
   * @param pageRequest request param container for paging, sorting, filtering
   * @param headword headword of entities
   * @return result as paged response
   */
  default PageResponse<Entity> findEntitiesByHeadword(Headword headword, PageRequest pageRequest) {
    if (headword == null) {
      return null;
    }
    return findEntitiesByHeadword(headword.getUuid(), pageRequest);
  }

  PageResponse<Entity> findEntitiesByHeadword(UUID headwordUuid, PageRequest pageRequest);

  default PageResponse<Headword> findHeadwordsForEntity(Entity entity, PageRequest pageRequest) {
    if (entity == null) {
      return null;
    }
    return findHeadwordsForEntity(entity.getUuid(), pageRequest);
  }

  PageResponse<Headword> findHeadwordsForEntity(UUID entityUuid, PageRequest pageRequest);

  default void remove(Entity entity, Headword headword) {
    remove(entity.getUuid(), headword.getUuid());
  }

  void remove(UUID entityUuid, UUID headwordUuid);
}
