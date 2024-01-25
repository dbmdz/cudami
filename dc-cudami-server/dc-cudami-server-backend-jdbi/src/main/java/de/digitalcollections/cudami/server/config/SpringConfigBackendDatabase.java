package de.digitalcollections.cudami.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.server.backend.impl.jdbi.plugins.DcCommonsJdbiPlugin;
import de.digitalcollections.cudami.server.backend.impl.jdbi.plugins.JsonbJdbiPlugin;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.DbIdentifierMapper;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.LocalDateRangeMapper;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.MainSubTypeMapper;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.MainSubTypeMapper.TitleTypeMapper;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.TitleMapper;
import de.digitalcollections.iiif.model.jackson.IiifObjectMapper;
import de.digitalcollections.model.view.BreadcrumbNode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.spring5.JdbiFactoryBean;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/** Database configuration. */
@Configuration
@ComponentScan(basePackages = {"de.digitalcollections.cudami.server.backend.impl.jdbi"})
@EnableTransactionManagement
public class SpringConfigBackendDatabase {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringConfigBackendDatabase.class);

  @Autowired
  @Bean
  @Qualifier(value = "pds")
  public PersistentTokenRepository persistentTokenRepository(DataSource pds) {
    JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
    tokenRepository.setDataSource(pds);
    return tokenRepository;
  }

  @Bean
  @SuppressFBWarnings(
      value = "THROWS_METHOD_THROWS_CLAUSE_BASIC_EXCEPTION",
      justification = "Jdbi throws java.lang.Exception...")
  public Jdbi dbi(JdbiFactoryBean factory) throws Exception {
    Jdbi dbi = factory.getObject();
    if (dbi != null) {
      dbi.registerRowMapper(BeanMapper.factory(BreadcrumbNode.class));
    }
    return dbi;
  }

  @Bean
  public JdbiFactoryBean jdbi(DataSource ds, ObjectMapper objectMapper) {
    JdbiFactoryBean jdbiFactoryBean = new JdbiFactoryBean(ds);
    List plugins = new ArrayList();
    plugins.add(new SqlObjectPlugin());
    plugins.add(new PostgresPlugin());
    plugins.add(new DcCommonsJdbiPlugin());
    plugins.add(new JsonbJdbiPlugin(objectMapper));
    jdbiFactoryBean.setPlugins(plugins);
    return jdbiFactoryBean;
  }

  @Bean
  public DataSourceTransactionManager transactionManager(DataSource dataSource) {
    return new DataSourceTransactionManager(dataSource);
  }

  @Bean
  public DbIdentifierMapper dbIdentifierMapper() {
    return new DbIdentifierMapper();
  }

  @Bean
  public IiifObjectMapper iiifObjectMapper() {
    return new IiifObjectMapper();
  }

  @Bean
  public MainSubTypeMapper.ExpressionTypeMapper expressionTypeMapper() {
    return new MainSubTypeMapper.ExpressionTypeMapper();
  }

  @Bean
  public MainSubTypeMapper.TitleTypeMapper titleTypeMapper() {
    return new MainSubTypeMapper.TitleTypeMapper();
  }

  @Bean
  public TitleMapper titleMapper(ObjectMapper objectMapper, TitleTypeMapper titleTypeMapper) {
    return new TitleMapper(objectMapper, titleTypeMapper);
  }

  @Bean
  public LocalDateRangeMapper dateRangeMapper() {
    return new LocalDateRangeMapper();
  }
}
