package de.digitalcollections.cudami.server.config;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class SpringConfigBackendForTest {

  @Bean
  @Primary
  public DataSource dataSource() throws SQLException {
    Connection connection = Mockito.mock(Connection.class);
    DataSource dataSource = Mockito.mock(DataSource.class);
    Mockito.when(dataSource.getConnection()).thenReturn(connection);
    return dataSource;
  }
}
