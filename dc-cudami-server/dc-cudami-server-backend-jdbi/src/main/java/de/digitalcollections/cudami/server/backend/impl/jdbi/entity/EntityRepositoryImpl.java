package de.digitalcollections.cudami.server.backend.impl.jdbi.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.core.model.impl.paging.PageResponseImpl;
import de.digitalcollections.cudami.model.api.entity.Entity;
import de.digitalcollections.cudami.model.impl.entity.EntityImpl;
import de.digitalcollections.cudami.server.backend.api.repository.entity.EntityRepository;
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
public class EntityRepositoryImpl<E extends EntityImpl> extends AbstractPagingAndSortingRepositoryImpl implements EntityRepository<E> {

    @Autowired
    private Jdbi dbi;

    @Autowired
    ObjectMapper objectMapper;
    
    @Override
    public long count() {
        String sql = "SELECT count(*) FROM entities";
        long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOnly());
        return count;
    }

    @Override
    public E create() {
        return (E) new EntityImpl();
    }

    @Override
    public PageResponse<E> find(PageRequest pageRequest) {
        StringBuilder query = new StringBuilder("SELECT * FROM entities");

        addPageRequestParams(pageRequest, query);
        List<EntityImpl> result = dbi.withHandle(h -> h.createQuery(query.toString())
                .mapToBean(EntityImpl.class)
                .list());
        long total = count();
        PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
        return pageResponse;
    }

    @Override
    public E findOne(UUID uuid) {
        List<? extends Entity> list = dbi.withHandle(h -> h.createQuery(
                "SELECT * FROM entities WHERE uuid = :uuid")
                .bind("uuid", uuid)
                .mapToBean(EntityImpl.class)
                .list());
        if (list.isEmpty()) {
            return null;
        }
        return (E) list.get(0);
    }

    @Override
    protected String[] getAllowedOrderByFields() {
        return new String[]{"created", "entityType", "lastModified"};
    }

    @Override
    public E save(E entity) {
        entity.setCreated(LocalDateTime.now());
        entity.setLastModified(LocalDateTime.now());

        EntityImpl result = null;
        try {
            result = dbi.withHandle(h -> h
                    .createQuery("INSERT INTO entities(created, description, entity_type, label, last_modified, thumbnail, uuid) VALUES (:created, :description::JSONB, :entityType, :label::JSONB, :lastModified, :thumbnail::JSONB, :uuid) RETURNING *")
                    .bind("description", objectMapper.writeValueAsString(entity.getDescription()))
                    .bind("label", objectMapper.writeValueAsString(entity.getLabel()))
                    .bind("thumbnail", objectMapper.writeValueAsString(entity.getThumbnail()))
                    .bindBean(entity)
                    .mapToBean(EntityImpl.class)
                    .findOnly());
        } catch (JsonProcessingException ex) {
            Logger.getLogger(EntityRepositoryImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return (E) result;
    }

    @Override
    public E update(E entity) {
        entity.setLastModified(LocalDateTime.now());

        EntityImpl result = null;
        try {
            // do not update/left out from statement: created, uuid
            result = dbi.withHandle(h -> h
                    .createQuery("UPDATE entities SET description=:description::JSONB, entity_type=:entityType, label=:label::JSONB, last_modified=:lastModified, thumbnail=:thumbnail::JSONB WHERE uuid=:uuid RETURNING *")
                    .bind("description", objectMapper.writeValueAsString(entity.getDescription()))
                    .bind("label", objectMapper.writeValueAsString(entity.getLabel()))
                    .bind("thumbnail", objectMapper.writeValueAsString(entity.getThumbnail()))
                    .bindBean(entity)
                    .mapToBean(EntityImpl.class)
                    .findOnly());
        } catch (JsonProcessingException ex) {
            Logger.getLogger(EntityRepositoryImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return (E) result;
    }
}
