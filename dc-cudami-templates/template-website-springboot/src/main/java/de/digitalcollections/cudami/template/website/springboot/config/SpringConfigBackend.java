package de.digitalcollections.cudami.template.website.springboot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.spring.config.SpringConfigCudami;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(SpringConfigCudami.class)
public class SpringConfigBackend implements InitializingBean {

  @Autowired
  ObjectMapper objectMapper;

  @Override
  public void afterPropertiesSet() throws Exception {
    // customize default spring boot jackson objectmapper
    DigitalCollectionsObjectMapper.customize(objectMapper);
  }
}
