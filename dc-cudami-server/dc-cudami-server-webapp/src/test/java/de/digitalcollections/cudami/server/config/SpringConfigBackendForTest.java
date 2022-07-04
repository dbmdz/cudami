package de.digitalcollections.cudami.server.config;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierTypeRepository;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
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

  @Bean
  @Primary
  public IdentifierTypeRepository identifierTypeRepository() throws RepositoryException {
    IdentifierTypeRepository identifierTypeRepository =
        Mockito.mock(IdentifierTypeRepository.class);
    Mockito.when(identifierTypeRepository.findAll()).thenReturn(new ArrayList<>());
    return identifierTypeRepository;
  }
}
