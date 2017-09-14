package de.digitalcollections.cudami.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.model.jackson.CudamiObjectMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
  "de.digitalcollections.cudami.client.backend.impl.repository"
})
//@PropertySource(value = {
//  "classpath:de/digitalcollections/cudami/config/SpringConfigBackend-${spring.profiles.active:local}.properties"
//})
public class SpringConfigBackend implements InitializingBean {

  @Autowired
  ObjectMapper objectMapper;

  @Override
  public void afterPropertiesSet() throws Exception {
    // customize default spring boot jackson objectmapper
    CudamiObjectMapper.customize(objectMapper);
  }
}
