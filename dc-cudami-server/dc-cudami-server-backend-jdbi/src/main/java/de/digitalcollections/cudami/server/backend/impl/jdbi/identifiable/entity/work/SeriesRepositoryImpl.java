package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.SeriesRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.AgentRepositoryImpl;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.manifestation.Series;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository("seriesRepository")
public class SeriesRepositoryImpl extends WorkRepositoryImpl<Series> implements SeriesRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "ser";
  public static final String TABLE_ALIAS = "ser";
  public static final String TABLE_NAME = "series";

  public SeriesRepositoryImpl(
      Jdbi dbi,
      @Qualifier("agentRepository") AgentRepositoryImpl<Agent> agentRepositoryImpl,
      ItemRepositoryImpl itemRepositoryImpl,
      CudamiConfig cudamiConfig) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Series.class,
        agentRepositoryImpl,
        itemRepositoryImpl,
        cudamiConfig.getOffsetForAlternativePaging());
  }
}
