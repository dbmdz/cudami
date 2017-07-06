package de.digitalcollections.cudami.client.webapp.aop;

import de.digitalcollections.commons.springaop.AbstractAopMethodLogger;
import org.aspectj.lang.annotation.Pointcut;

public class MyAopMethodLogger extends AbstractAopMethodLogger {

  // TODO change packagenames for business and backend
  @Pointcut("execution(public * de.digitalcollections.cms.client.webapp.controller..*Controller.*(..)) || "
          + "execution(public * org.mdz.dzp.admin.business.impl.service..*Service.*(..)) || "
          + "execution(public * org.mdz.dzp.admin.backend.impl.jpa.repository..*.*Repository.*(..))")
  @Override
  public void methodsToBeLogged() {
  }
}
