package de.digitalcollections.cudami.server.backend.impl.jdbi.semantic;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.semantic.TagRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.semantic.Tag;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.springframework.stereotype.Repository;

@Repository
public class TagRepositoryImpl extends JdbiRepositoryImpl implements TagRepository {

  // TODO
  public static final String TABLE_NAME = "tags";
  public static final String TABLE_ALIAS = "ts";
  public static final String MAPPING_PREFIX = "ts";

  public TagRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX, cudamiConfig.getOffsetForAlternativePaging());
    this.dbi.registerRowMapper(BeanMapper.factory(Tag.class, MAPPING_PREFIX));
  }

  @Override
  public Tag getByUuid(UUID uuid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Tag save(Tag tag) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Tag update(Tag tag) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean delete(List<UUID> uuids) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public PageResponse<Tag> find(PageRequest pageRequest) {
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
