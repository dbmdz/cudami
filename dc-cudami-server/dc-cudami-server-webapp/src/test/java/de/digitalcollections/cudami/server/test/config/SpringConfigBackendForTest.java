package de.digitalcollections.cudami.server.test.config;

import javax.sql.DataSource;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SpringConfigBackendForTest {

  @Primary
  @Bean
  public DataSource dataSource() {
    return Mockito.mock(DataSource.class);
  }
}
