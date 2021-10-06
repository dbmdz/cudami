package de.digitalcollections.cudami.server.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringUtility implements ApplicationContextAware {

  @Autowired private static ApplicationContext applicationContext;

  @SuppressFBWarnings(value = {"ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD"})
  public void setApplicationContext(final ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  /*
     Get a class bean from the application context
  */
  public static <T> T getBean(final Class clazz) {
    return (T) applicationContext.getBean(clazz);
  }

  /*
     Return the application context if necessary for anything else
  */
  public static ApplicationContext getContext() {
    return applicationContext;
  }
}
