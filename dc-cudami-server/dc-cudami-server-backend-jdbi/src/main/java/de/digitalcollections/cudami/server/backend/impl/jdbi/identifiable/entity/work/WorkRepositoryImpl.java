package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.WorkRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.model.identifiable.entity.work.Work;
import java.util.Set;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("workRepository")
public class WorkRepositoryImpl extends EntityRepositoryImpl<Work> implements WorkRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "wo";
  public static final String TABLE_ALIAS = "w";
  public static final String TABLE_NAME = "works";

  @Override
  public String getSqlInsertFields() {
    return super.getSqlInsertFields() + ", date_published, timevalue_published";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  @Override
  public String getSqlInsertValues() {
    return super.getSqlInsertValues() + ", :datePublished, :timeValuePublished::JSONB";
  }

  @Override
  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".date_published "
        + mappingPrefix
        + "_datePublished, "
        + tableAlias
        + ".timevalue_published "
        + mappingPrefix
        + "_timeValuePublished";
  }

  @Override
  public String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues()
        + ", date_published=:datePublished, timevalue_published=:timeValuePublished::JSONB";
  }

  @Autowired
  public WorkRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    this(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Work.class,
        cudamiConfig.getOffsetForAlternativePaging());
  }

  public WorkRepositoryImpl(
      Jdbi dbi,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      Class<? extends Work> workImplClass,
      int offsetForAlternativePaging) {
    super(dbi, tableName, tableAlias, mappingPrefix, workImplClass, offsetForAlternativePaging);
  }

  @Override
  public Set<Work> getWorksForItem(UUID itemUuid) {
    // FIXME
    return null;
  }
}
