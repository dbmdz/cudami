package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.PublicationRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.model.identifiable.entity.work.Publication;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.springframework.stereotype.Repository;

@Repository
public class PublicationRepositoryImpl extends JdbiRepositoryImpl implements PublicationRepository {

  // This is still a big TODO

  public static final String TABLE_NAME = "publications";
  public static final String TABLE_ALIAS = "publ";
  public static final String MAPPING_PREFIX = "publ";

  public PublicationRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX, cudamiConfig.getOffsetForAlternativePaging());
    this.dbi.registerRowMapper(BeanMapper.factory(Publication.class, MAPPING_PREFIX));
  }

  @Override
  public Publication getByUuid(UUID uuid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Publication save(Publication publication) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Publication update(Publication publication) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean delete(List<UUID> uuids) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public PageResponse<Publication> find(PageRequest pageRequest) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getColumnName(String modelProperty) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected String getUniqueField() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected boolean supportsCaseSensitivityForProperty(String modelProperty) {
    // TODO Auto-generated method stub
    return false;
  }
}
