package de.digitalcollections.cudami.server.config;

import javax.sql.DataSource;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class SpringConfigBackendForTest {

  @Bean
  @Primary
  public DataSource dataSource() {
    return Mockito.mock(DataSource.class);
  }
}
