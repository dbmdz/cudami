package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.InvolvementRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.model.identifiable.entity.work.Involvement;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.springframework.stereotype.Repository;

@Repository
public class InvolvementRepositoryImpl extends JdbiRepositoryImpl implements InvolvementRepository {

  public static final String TABLE_NAME = "involvements"; // TODO
  public static final String TABLE_ALIAS = "invo";
  public static final String MAPPING_PREFIX = "invo";

  public InvolvementRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX, cudamiConfig.getOffsetForAlternativePaging());
    this.dbi.registerRowMapper(BeanMapper.factory(Involvement.class, MAPPING_PREFIX));
  }

  @Override
  public Involvement getByUuid(UUID uuid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Involvement save(Involvement involvement) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Involvement update(Involvement involvement) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean delete(List<UUID> uuids) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public PageResponse<Involvement> find(PageRequest pageRequest) {
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
