package de.digitalcollections.cudami.server.backend.impl.database;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

public class SchemaGenerator {

  private final Configuration cfg;

  /**
   * Example: java SchemaGenerator de.digitalcollections.cudami.client.backend.impl.jpa.entity
   * src/main/resources/sql/generated/
   *
   * @param args first argument is the package to scan for entities, second argument directory to generate the dll to
   * @throws java.lang.Exception
   */
  public static void main(String[] args) throws Exception {
    final String packageName = args[0];
    SchemaGenerator gen = new SchemaGenerator(packageName);
    final String directory = args[1];
    if (directory != null) {
      File dir = new File(directory);
      if (!dir.exists()) {
        dir.mkdirs();
      }
    }
    gen.generate(Dialect.POSTGRESQL, directory);
    gen.generate(Dialect.MYSQL, directory);
    gen.generate(Dialect.ORACLE, directory);
    gen.generate(Dialect.HSQL, directory);
    gen.generate(Dialect.H2, directory);
  }

  public SchemaGenerator(String packageName) throws Exception {
    cfg = new Configuration();
    cfg.setProperty("hibernate.hbm2ddl.auto", "create");

//        for (Class clazz : getClasses(packageName)) {
//            cfg.addAnnotatedClass(clazz);
//        }
    List<Class> entitiesInPackage = findAnnotatedClassesInPackage(packageName, Entity.class);
    for (Class clazz : entitiesInPackage) {
      cfg.addAnnotatedClass(clazz);
    }
  }

  /**
   * Utility method used to fetch Class list based on a package name.
   *
   * @param packageName (should be the package containing your annotated beans.
   */
  private List<Class> getClasses(String packageName) throws Exception {
    File directory = null;
    try {
      ClassLoader cld = getClassLoader();
      URL resource = getResource(packageName, cld);
      directory = new File(resource.getFile());
    } catch (NullPointerException ex) {
      throw new ClassNotFoundException(packageName + " (" + directory
              + ") does not appear to be a valid package");
    }
    return collectClasses(packageName, directory);
  }

  private ClassLoader getClassLoader() throws ClassNotFoundException {
    ClassLoader cld = Thread.currentThread().getContextClassLoader();
    if (cld == null) {
      throw new ClassNotFoundException("Can't get class loader.");
    }
    return cld;
  }

  private URL getResource(String packageName, ClassLoader cld) throws ClassNotFoundException {
    String path = packageName.replace('.', '/');
    URL resource = cld.getResource(path);
    if (resource == null) {
      throw new ClassNotFoundException("No resource for " + path);
    }
    return resource;
  }

  private List<Class> collectClasses(String packageName, File directory) throws ClassNotFoundException {
    List<Class> classes = new ArrayList<>();
    if (directory.exists()) {
      String[] files = directory.list();
      for (String file : files) {
        if (file.endsWith(".class")) {
          // removes the .class extension
          classes.add(Class.forName(packageName + '.'
                  + file.substring(0, file.length() - 6)));
        }
      }
    } else {
      throw new ClassNotFoundException(packageName
              + " is not a valid package");
    }
    return classes;
  }

  /**
   * Method that actually creates the file.
   *
   * @param dialect to use
   */
  private void generate(Dialect dialect, String directory) {
    cfg.setProperty("hibernate.dialect", dialect.getDialectClass());
    SchemaExport export = new SchemaExport(cfg);
    export.setDelimiter(";");
    export.setOutputFile(directory + "ddl_" + dialect.name().toLowerCase() + ".sql");
    export.setFormat(true);
    export.execute(true, false, false, false);
  }

  /**
   * Holds the classnames of hibernate dialects for easy reference.
   */
  private static enum Dialect {

    POSTGRESQL("org.hibernate.dialect.PostgreSQLDialect"),
    ORACLE("org.hibernate.dialect.Oracle10gDialect"),
    MYSQL("org.hibernate.dialect.MySQLDialect"),
    HSQL("org.hibernate.dialect.HSQLDialect"),
    H2("org.hibernate.dialect.H2Dialect");

    private final String dialectClass;

    private Dialect(String dialectClass) {
      this.dialectClass = dialectClass;
    }

    public String getDialectClass() {
      return dialectClass;
    }
  }

  private List<Class> findAnnotatedClassesInPackage(String basePackage, Class annotationClass) throws IOException, ClassNotFoundException {
    ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

    List<Class> candidates = new ArrayList<>();
    String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
            + resolveBasePackage(basePackage) + "/" + "**/*.class";
    Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
    for (Resource resource : resources) {
      if (resource.isReadable()) {
        MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
        if (isCandidate(metadataReader, annotationClass)) {
          candidates.add(Class.forName(metadataReader.getClassMetadata().getClassName()));
        }
      }
    }
    return candidates;
  }

  private String resolveBasePackage(String basePackage) {
    return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
  }

  private boolean isCandidate(MetadataReader metadataReader, Class annotationClass) throws ClassNotFoundException {
    try {
      Class c = Class.forName(metadataReader.getClassMetadata().getClassName());
      if (c.getAnnotation(annotationClass) != null) {
        return true;
      }
    } catch (ClassNotFoundException e) {
    }
    return false;
  }
}
