package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.ExpressionTypeRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.model.identifiable.entity.work.ExpressionType;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ExpressionTypeRepositoryImpl extends JdbiRepositoryImpl
    implements ExpressionTypeRepository {

  public static final String TABLE_NAME = "expressiontypes"; // TODO
  public static final String TABLE_ALIAS = "expt";
  public static final String MAPPING_PREFIX = "expt";

  public ExpressionTypeRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX, cudamiConfig.getOffsetForAlternativePaging());
    this.dbi.registerRowMapper(BeanMapper.factory(ExpressionType.class, MAPPING_PREFIX));
  }

  @Override
  public ExpressionType getByUuid(UUID uuid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ExpressionType save(ExpressionType expressionType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ExpressionType update(ExpressionType expressionType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean delete(List<UUID> uuids) {
    // TODO Auto-generated method stub
    return false;
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

  @Override
  public PageResponse<ExpressionType> find(PageRequest pageRequest) {
    // TODO Auto-generated method stub
    return null;
  }
}
