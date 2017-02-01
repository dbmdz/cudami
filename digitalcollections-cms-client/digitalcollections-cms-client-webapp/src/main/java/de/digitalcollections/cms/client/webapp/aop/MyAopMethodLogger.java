package de.digitalcollections.cms.client.webapp.aop;

import de.digitalcollections.commons.springaop.AbstractAopMethodLogger;
import org.aspectj.lang.annotation.Pointcut;

public class MyAopMethodLogger extends AbstractAopMethodLogger {

  @Pointcut("execution(public * org.mdz.dzp.admin.frontend.webapp.controller..*Controller.*(..)) || "
          + "execution(public * org.mdz.dzp.admin.business.impl.service..*Service.*(..)) || "
          + "execution(public * org.mdz.dzp.admin.backend.impl.jpa.repository..*.*Repository.*(..))")
  @Override
  public void methodsToBeLogged() {
  }
}
