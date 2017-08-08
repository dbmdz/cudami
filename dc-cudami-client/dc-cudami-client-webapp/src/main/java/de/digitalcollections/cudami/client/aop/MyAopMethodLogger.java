package de.digitalcollections.cudami.client.aop;

import de.digitalcollections.commons.springaop.AbstractAopMethodLogger;
import org.aspectj.lang.annotation.Pointcut;

public class MyAopMethodLogger extends AbstractAopMethodLogger {

  @Pointcut("execution(public * de.digitalcollections.cudami.client.controller..*Controller.*(..)) || "
          + "execution(public * de.digitalcollections.cudami.client.business.impl.service..*Service.*(..)) || "
          + "execution(public * de.digitalcollections.cudami.client.backend.impl.repository..*.*Repository.*(..))")
  @Override
  public void methodsToBeLogged() {
  }
}
