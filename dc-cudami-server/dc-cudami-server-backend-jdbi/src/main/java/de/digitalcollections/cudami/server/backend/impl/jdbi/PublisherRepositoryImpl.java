package de.digitalcollections.cudami.server.backend.impl.jdbi;

import de.digitalcollections.cudami.server.backend.api.repository.PublisherRepository;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.identifiable.entity.work.Publisher;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;

public class PublisherRepositoryImpl extends JdbiRepositoryImpl implements PublisherRepository {

  public PublisherRepositoryImpl(
      Jdbi dbi,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      int offsetForAlternativePaging) {
    super(dbi, tableName, tableAlias, mappingPrefix, offsetForAlternativePaging);
  }

  @Override
  public PageResponse<Publisher> find(PageRequest pageRequest) throws RepositoryException {
    return null;
  }

  @Override
  public Publisher getByUuid(UUID uuid) throws RepositoryException {
    return null;
  }

  @Override
  public Publisher save(Publisher publisher) throws RepositoryException {
    return null;
  }

  @Override
  public Publisher update(Publisher publisher) throws RepositoryException {
    return null;
  }

  @Override
  public int deleteByUuid(UUID uuid) throws RepositoryException {
    return 0;
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    return null;
  }

  @Override
  public String getColumnName(String modelProperty) {
    return null;
  }

  @Override
  protected String getUniqueField() {
    return null;
  }

  @Override
  protected boolean supportsCaseSensitivityForProperty(String modelProperty) {
    return false;
  }
}
