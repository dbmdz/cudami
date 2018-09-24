package de.digitalcollections.cudami.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.commons.jdbi.DcCommonsJdbiPlugin;
import de.digitalcollections.cudami.server.backend.impl.jdbi.plugins.JsonbJdbiPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.DataSourceConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.spring4.JdbiFactoryBean;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

/**
 * Database configuration.
 */
@Configuration
//@EnableTransactionManagement
@ComponentScan(basePackages = {
  "de.digitalcollections.cudami.server.backend.impl.jdbi"
})
public class SpringConfigBackendDatabase {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringConfigBackendDatabase.class);

  @Value("${cudami.database.name}")
  private String databaseName;

  @Value("${cudami.database.hostname}")
  private String databaseHostname;

  @Value("${cudami.database.password}")
  private String databasePassword;

  @Value("${cudami.database.port}")
  private String databasePort;

  @Value("${cudami.database.username}")
  private String databaseUsername;

  @Autowired
  ObjectMapper objectMapper;

  @Bean(initMethod = "migrate")
  @Autowired
  @Qualifier(value = "pds")
  public Flyway flyway(DataSource pds) {
    Flyway flyway = new Flyway();
    flyway.setDataSource(pds); // could be another datasource with different user/pwd...
    flyway.setLocations("classpath:/de/digitalcollections/cudami/server/backend/impl/database/migration");
    flyway.setBaselineOnMigrate(true);
    return flyway;
  }

  @Bean
  @Autowired
  @Qualifier(value = "pds")
  public PersistentTokenRepository persistentTokenRepository(DataSource pds) {
    JdbcTokenRepositoryImpl db = new JdbcTokenRepositoryImpl();
    db.setDataSource(pds);
    return db;
  }

  @Bean
  @DependsOn(value = "flyway")
  @Autowired
  @Qualifier(value = "ds")
  public JdbiFactoryBean jdbi(DataSource ds) throws Exception {
    JdbiFactoryBean jdbiFactoryBean = new JdbiFactoryBean(ds);
    List plugins = new ArrayList();
    plugins.add(new SqlObjectPlugin());
    plugins.add(new PostgresPlugin());
    plugins.add(new DcCommonsJdbiPlugin());
    plugins.add(new JsonbJdbiPlugin(objectMapper));
    jdbiFactoryBean.setPlugins(plugins);
    return jdbiFactoryBean;
  }

//  @Bean
//  public DataSourceTransactionManager transactionManager() {
//    return new DataSourceTransactionManager(dataSource());
//  }

  /*
   * Unpooled datasource.
   */
  @Bean(name = "ds")
  @Primary
  public DataSource dataSource() {
    DriverManagerDataSource ds = new DriverManagerDataSource();
    ds.setDriverClassName("org.postgresql.Driver");
    ds.setUrl("jdbc:postgresql://" + databaseHostname + ":" + databasePort + "/" + databaseName);
    ds.setUsername(databaseUsername);
    ds.setPassword(databasePassword);
    ds.setConnectionProperties(getConnectionProperties());
    return ds;
  }

  private Properties getConnectionProperties() {
    Properties props = new Properties();
    props.put("tcpKeepAlive", "true");
    // Enable or disable TCP keep-alive probe. The default is false.

    props.put("testOnBorrow", "true");
    // default: true. The indication of whether objects will be validated before being borrowed from the pool.
    // If the object fails to validate, it will be dropped from the pool, and we will attempt to borrow another.
    props.put("testOnReturn", "true");
    props.put("testWhileIdle", "true");

    props.put("validationQuery", "SELECT 1");
    // validationQuery - The SQL query that will be used to validate connections from this pool before returning
    // them to the caller. If specified, this query MUST be an SQL SELECT statement that returns at least one row.
    // If not specified, connections will be validation by calling the isValid() method.

    props.put("timeBetweenEvictionRunsMillis", "60000"); // default is disabled
    props.put("numTestsPerEvictionRun", "3"); // default
    props.put("minEvictableIdleTimeMillis", "1800000"); // =1000 * 60 * 30  (=default)

    // sb.append(";connectTimeout=10");
    // The timeout value used for socket connect operations.
    // If connecting to the server takes longer than this value, the connection is broken.
    // The timeout is specified in seconds and a value of zero means that it is disabled.
    // sb.append(";socketTimeout=30");
    // The timeout value used for socket read operations.
    // If reading from the server takes longer than this value, the connection is closed.
    // This can be used as both a brute force global query timeout and a method of detecting network problems.
    // The timeout is specified in seconds and a value of zero means that it is disabled.
    return props;
  }

  /*
     * Pooled datasource.
     * We create the PoolingDataSource, passing in the object pool created.
   */
  @Bean(name = "pds")
  @Autowired
  @Qualifier(value = "ds")
  public DataSource pooledDataSource(DataSource ds) {
    ObjectPool<PoolableConnection> pool = getObjectPool(ds);
    PoolingDataSource pds = new PoolingDataSource<>(pool);
    return pds;
  }

  /*
     * We need a ObjectPool that serves as the actual pool of connections.
     * We'll use a GenericObjectPool instance, although any ObjectPool implementation will suffice.
   */
  private ObjectPool<PoolableConnection> getObjectPool(DataSource ds) {
    PoolableConnectionFactory poolableConnectionFactory = getPoolableConnectionFactory(ds);
    ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(poolableConnectionFactory);
    // Set the factory's pool property to the owning pool
    poolableConnectionFactory.setPool(connectionPool);
    return connectionPool;
  }

  /*
     * We create the PoolableConnectionFactory, which wraps the "real" Connections
     * created by the ConnectionFactory with the classes that implement the pooling functionality.
   */
  private PoolableConnectionFactory getPoolableConnectionFactory(DataSource ds) {
    DataSourceConnectionFactory dataSourceConnectionFactory = getDataSourceConnectionFactory(ds);
    PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(dataSourceConnectionFactory, null);
    poolableConnectionFactory.setValidationQuery("SELECT 1");
    long maxConnLifetimeMillis = 600000; // ten minutes
    poolableConnectionFactory.setMaxConnLifetimeMillis(maxConnLifetimeMillis);
    return poolableConnectionFactory;
  }

  /*
     * We create a ConnectionFactory that the pool will use to create Connections.
     * It is using the unppoled datasource we had before.
   */
  private DataSourceConnectionFactory getDataSourceConnectionFactory(DataSource ds) {
    DataSourceConnectionFactory dscf = new DataSourceConnectionFactory(ds);
    return dscf;
  }

}
