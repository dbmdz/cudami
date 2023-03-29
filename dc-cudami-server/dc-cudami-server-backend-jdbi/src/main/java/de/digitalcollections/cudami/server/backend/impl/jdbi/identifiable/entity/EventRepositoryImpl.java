package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EventRepository;
import de.digitalcollections.model.identifiable.entity.Event;
import java.util.List;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

@Repository
public class EventRepositoryImpl extends EntityRepositoryImpl<Event> implements EventRepository {

  public static final String MAPPING_PREFIX = "ev";
  public static final String TABLE_ALIAS = "ev";
  public static final String TABLE_NAME = "events";

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
  public Event create() throws RepositoryException {
    return new Event();
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    List<String> orderByFields = super.getAllowedOrderByFields();
    orderByFields.add("name");
    return orderByFields;
  }

  @Override
  public String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "name":
        return tableAlias + ".name";
      default:
        return super.getColumnName(modelProperty);
    }
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
