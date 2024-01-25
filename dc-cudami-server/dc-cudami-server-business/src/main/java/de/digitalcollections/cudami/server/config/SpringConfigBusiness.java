package de.digitalcollections.cudami.server.config;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.model.util.SlugGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan(
    basePackages = {
      "de.digitalcollections.cudami.model",
      "de.digitalcollections.cudami.server.business.impl.service",
      "de.digitalcollections.cudami.server.business.impl.validator"
    })
@EnableTransactionManagement
public class SpringConfigBusiness {

  @Autowired private CudamiConfig cudamiConfig;

  @Bean
  public SlugGenerator slugGenerator() {
    SlugGenerator slugGenerator = new SlugGenerator();
    slugGenerator.setMaxLength(cudamiConfig.getUrlAlias().getMaxLength());
    return slugGenerator;
  }
}
