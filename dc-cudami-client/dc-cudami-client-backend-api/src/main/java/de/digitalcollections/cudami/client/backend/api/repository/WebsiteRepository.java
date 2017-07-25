package de.digitalcollections.cudami.client.backend.api.repository;

import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.cudami.model.api.entity.Website;
import java.io.Serializable;
import java.util.List;

/**
 * Repository for Website persistence handling.
 * @param <S> unique serializable identifier
 * @param <W> implementation of Website interface
 */
public interface WebsiteRepository<W extends Website, S extends Serializable> {

  W create();

  PageResponse<W> find(PageRequest pageRequest);

  List<W> findAll();

  W findOne(Long id);
//  T find(UUID uuid);

  W save(W website);
}
