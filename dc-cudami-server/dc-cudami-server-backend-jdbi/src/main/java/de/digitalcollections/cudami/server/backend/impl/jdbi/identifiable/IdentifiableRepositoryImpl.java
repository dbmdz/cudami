package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.core.model.impl.paging.PageResponseImpl;
import de.digitalcollections.cudami.model.api.identifiable.Identifiable;
import de.digitalcollections.cudami.model.impl.identifiable.IdentifiableImpl;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractPagingAndSortingRepositoryImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class IdentifiableRepositoryImpl<I extends IdentifiableImpl> extends AbstractPagingAndSortingRepositoryImpl implements IdentifiableRepository<I> {

  @Autowired
  private Jdbi dbi;

  @Autowired
  ObjectMapper objectMapper;

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM identifiables";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOnly());
    return count;
  }

  @Override
  public I create() {
    return (I) new IdentifiableImpl();
  }

  @Override
  public PageResponse<I> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder("SELECT * FROM identifiables");

    addPageRequestParams(pageRequest, query);
    List<IdentifiableImpl> result = dbi.withHandle(h -> h.createQuery(query.toString())
            .mapToBean(IdentifiableImpl.class)
            .list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public I findOne(UUID uuid) {
    List<? extends Identifiable> list = dbi.withHandle(h -> h.createQuery(
            "SELECT * FROM identifiables WHERE uuid = :uuid")
            .bind("uuid", uuid)
            .mapToBean(IdentifiableImpl.class)
            .list());
    if (list.isEmpty()) {
      return null;
    }
    return (I) list.get(0);
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[]{"created", "type", "lastModified"};
  }

  @Override
  public I save(I identifiable) {
    identifiable.setCreated(LocalDateTime.now());
    identifiable.setLastModified(LocalDateTime.now());

    IdentifiableImpl result = null;
    try {
      result = dbi.withHandle(h -> h
              .createQuery("INSERT INTO identifiables(created, description, identifiable_type, label, last_modified, iiif_image, uuid) VALUES (:created, :description::JSONB, :type, :label::JSONB, :lastModified, :iiifImage::JSONB, :uuid) RETURNING *")
              .bind("description", objectMapper.writeValueAsString(identifiable.getDescription()))
              .bind("label", objectMapper.writeValueAsString(identifiable.getLabel()))
              .bind("iiifImage", objectMapper.writeValueAsString(identifiable.getIiifImage()))
              .bindBean(identifiable)
              .mapToBean(IdentifiableImpl.class)
              .findOnly());
    } catch (JsonProcessingException ex) {
      Logger.getLogger(IdentifiableRepositoryImpl.class.getName()).log(Level.SEVERE, null, ex);
    }
    return (I) result;
  }

  @Override
  public I update(I identifiable) {
    identifiable.setLastModified(LocalDateTime.now());

    IdentifiableImpl result = null;
    try {
      // do not update/left out from statement: created, uuid
      result = dbi.withHandle(h -> h
              .createQuery("UPDATE identifiables SET description=:description::JSONB, identifiable_type=:type, label=:label::JSONB, last_modified=:lastModified, iiif_image=:iiifImage::JSONB WHERE uuid=:uuid RETURNING *")
              .bind("description", objectMapper.writeValueAsString(identifiable.getDescription()))
              .bind("label", objectMapper.writeValueAsString(identifiable.getLabel()))
              .bind("iiifImage", objectMapper.writeValueAsString(identifiable.getIiifImage()))
              .bindBean(identifiable)
              .mapToBean(IdentifiableImpl.class)
              .findOnly());
    } catch (JsonProcessingException ex) {
      Logger.getLogger(IdentifiableRepositoryImpl.class.getName()).log(Level.SEVERE, null, ex);
    }
    return (I) result;
  }
}
