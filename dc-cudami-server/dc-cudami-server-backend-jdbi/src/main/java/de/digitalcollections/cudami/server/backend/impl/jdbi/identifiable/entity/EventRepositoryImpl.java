package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EventRepository;
import de.digitalcollections.model.identifiable.entity.Event;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class EventRepositoryImpl extends EntityRepositoryImpl<Event> implements EventRepository {

  public static final String TABLE_NAME = "events";
  public static final String TABLE_ALIAS = "ev";
  public static final String MAPPING_PREFIX = "ev";

  @Autowired
  public EventRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Event.class,
        cudamiConfig.getOffsetForAlternativePaging());
  }

  @Override
  public boolean delete(List<UUID> uuids) {
    if (uuids == null || uuids.isEmpty()) {
      return true;
    }

    int deletions =
        dbi.withHandle(
            h ->
                h.createUpdate("DELETE FROM " + tableName + " WHERE uuid in (<uuids>)")
                    .bindList("uuids", uuids)
                    .execute());
    return deletions == uuids.size();
  }

  @Override
  public String getColumnName(String modelProperty) {
    switch (modelProperty) {
      case "name":
        return modelProperty;
      default:
        return super.getColumnName(modelProperty);
    }
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    List<String> orderByFields = super.getAllowedOrderByFields();
    orderByFields.add("name");
    return orderByFields;
  }

  @Override
  protected boolean supportsCaseSensitivityForProperty(String modelProperty) {
    switch (modelProperty) {
      case "name":
        return false; // because of jsonb
      default:
        return super.supportsCaseSensitivityForProperty(modelProperty);
    }
  }
}