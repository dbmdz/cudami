package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.SeriesRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.AgentRepositoryImpl;
import de.digitalcollections.model.identifiable.entity.work.Series;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

@Repository("seriesRepository")
public class SeriesRepositoryImpl extends WorkRepositoryImpl<Series> implements SeriesRepository {

  public SeriesRepositoryImpl(
      Jdbi dbi,
      AgentRepositoryImpl agentRepositoryImpl,
      ItemRepositoryImpl itemRepositoryImpl,
      CudamiConfig cudamiConfig) {
    super(dbi, agentRepositoryImpl, itemRepositoryImpl, cudamiConfig);
  }
}
