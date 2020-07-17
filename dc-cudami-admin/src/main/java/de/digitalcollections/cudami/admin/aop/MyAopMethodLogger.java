package de.digitalcollections.cudami.admin.aop;

import de.digitalcollections.commons.springaop.AbstractAopMethodLogger;
import org.aspectj.lang.annotation.Pointcut;

public class MyAopMethodLogger extends AbstractAopMethodLogger {

  @Pointcut(
      "execution(public * de.digitalcollections.cudami.admin.controller..*Controller.*(..)) || "
          + "execution(public * de.digitalcollections.cudami.admin.business.impl.service..*Service.*(..)) || "
          + "execution(public * de.digitalcollections.cudami.admin.backend.impl.repository..*.*Repository.*(..))")
  @Override
  public void methodsToBeLogged() {}
}
