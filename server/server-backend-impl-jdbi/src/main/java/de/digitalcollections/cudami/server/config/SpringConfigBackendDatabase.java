package de.digitalcollections.cudami.server.config;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.DataSourceConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.spring4.JdbiFactoryBean;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Database configuration.
 */
@Configuration
@EnableTransactionManagement
public class SpringConfigBackendDatabase {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringConfigBackendDatabase.class);

  @Value("${database.name}")
  private String databaseName;

  @Value("${database.hostname}")
  private String databaseHostname;

  @Value("${database.password}")
  private String databasePassword;

  @Value("${database.port}")
  private String databasePort;

  @Value("${database.username}")
  private String databaseUsername;

  @Bean(initMethod = "migrate")
  public Flyway flyway() {
    Flyway flyway = new Flyway();
    flyway.setDataSource(pooledDataSource()); // could be another datasource with different user/pwd...
    flyway.setLocations("classpath:/de/digitalcollections/cudami/server/backend/impl/database/migration");
    flyway.setBaselineOnMigrate(true);
    return flyway;
  }

  @Bean
  public PersistentTokenRepository persistentTokenRepository() {
    JdbcTokenRepositoryImpl db = new JdbcTokenRepositoryImpl();
    db.setDataSource(pooledDataSource());
    return db;
  }

  @Bean
  @DependsOn(value = "flyway")
  public JdbiFactoryBean jdbi() {
    JdbiFactoryBean jdbiFactoryBean = new JdbiFactoryBean(pooledDataSource());
    Set plugins = Collections.singleton(new SqlObjectPlugin());
    jdbiFactoryBean.setPlugins(plugins);
    return jdbiFactoryBean;
  }

  /*
   * Unpooled datasource.
   */
  @Bean
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
  @Bean
  public DataSource pooledDataSource() {
    ObjectPool<PoolableConnection> pool = getObjectPool();
    PoolingDataSource ds = new PoolingDataSource<>(pool);
    return ds;
  }

  /*
     * We need a ObjectPool that serves as the actual pool of connections.
     * We'll use a GenericObjectPool instance, although any ObjectPool implementation will suffice.
   */
  private ObjectPool<PoolableConnection> getObjectPool() {
    PoolableConnectionFactory poolableConnectionFactory = getPoolableConnectionFactory();
    ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(poolableConnectionFactory);
    // Set the factory's pool property to the owning pool
    poolableConnectionFactory.setPool(connectionPool);
    return connectionPool;
  }

  /*
     * We create the PoolableConnectionFactory, which wraps the "real" Connections
     * created by the ConnectionFactory with the classes that implement the pooling functionality.
   */
  private PoolableConnectionFactory getPoolableConnectionFactory() {
    DataSourceConnectionFactory dataSourceConnectionFactory = getDataSourceConnectionFactory();
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
  private DataSourceConnectionFactory getDataSourceConnectionFactory() {
    DataSourceConnectionFactory dscf = new DataSourceConnectionFactory(dataSource());
    return dscf;
  }

}
