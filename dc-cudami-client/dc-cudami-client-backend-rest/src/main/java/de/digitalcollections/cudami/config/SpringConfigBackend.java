package de.digitalcollections.cudami.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.digitalcollections.core.model.jackson.DcCoreModelModule;
import de.digitalcollections.cudami.model.jackson.CudamiModule;
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
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.registerModule(new DcCoreModelModule());
    objectMapper.registerModule(new CudamiModule());
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }
}
